package com.dialpad

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.telecom.TelecomManager
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.dialpad.extensions.getMyContactsCursor
import com.dialpad.helpers.MyContactsContentProvider
import com.dialpad.helpers.SimpleContactsHelper
import com.dialpad.helpers.ContactHelper
import com.dialpad.helpers.ensureBackgroundThread
import com.dialpad.helpers.setCallForwarding
import com.facebook.react.module.annotations.ReactModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ReactModule(name = DialpadModule.NAME)
class DialpadModule(reactContext: ReactApplicationContext) :
  NativeDialpadSpec(reactContext), ActivityEventListener {

  private val dialerResultCallbacks = SparseArray<Promise>()
  private var dialerRequestCode = 9876

  override fun getName(): String {
    return NAME
  }

  init {
    CallEventRepository.initialize(reactContext)
    reactContext.addActivityEventListener(this)
  }

  override fun onActivityResult(
    activity: Activity,
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    val promise = dialerResultCallbacks.get(requestCode) ?: return
    if (resultCode == Activity.RESULT_OK) {
      promise.resolve("Accepted")
    } else {
      promise.reject("Rejected", "User did not accept default dialer")
    }
    dialerResultCallbacks.remove(requestCode)
  }

  override fun onNewIntent(intent: Intent) {
    TODO("Not yet implemented")
  }



  @RequiresApi(Build.VERSION_CODES.Q)
  @ReactMethod
  override  fun requestRole(promise: Promise) {
    val roleManager = reactApplicationContext.getSystemService(RoleManager::class.java)
    val telecomManager = reactApplicationContext.getSystemService(TelecomManager::class.java)
    val packageName = reactApplicationContext.packageName

    if (roleManager == null) {
      promise.reject("TelecomError", "TelecomManager not available")
      return
    }

    if (telecomManager.defaultDialerPackage == packageName) {
      promise.resolve("Already Default Dialer")
      return
    }

    val activity = reactApplicationContext.currentActivity
    if (activity == null) {
      promise.reject("ActivityError", "No current activity")
      return
    }

    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)

    dialerResultCallbacks.put(dialerRequestCode, promise)
    reactApplicationContext.startActivityForResult(intent, dialerRequestCode, null)
    dialerRequestCode++
  }
  @RequiresPermission(anyOf = [Manifest.permission.CALL_PHONE, Manifest.permission.MANAGE_OWN_CALLS])
  @ReactMethod
  override fun makeCall(phoneNumber: String, promise: Promise) {
    try {
      if (phoneNumber.isEmpty()) {
        promise.reject("INVALID_NUMBER", "Phone number is missing or invalid")
      }
      val telecomManager = reactApplicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
      val uri = Uri.fromParts("tel", phoneNumber, null)
      if (reactApplicationContext.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        telecomManager.placeCall(uri, null)
        promise.resolve("Call placed successfully")
      } else {
        promise.reject("PERMISSION_DENIED", "Permission to make calls not granted")
      }
    } catch (e: Exception) {
      promise.reject("CALL_FAILED", "Failed to place the call: ${e.message}")
    }
  }

  @ReactMethod
  override fun toggleSecureNumber(value: Boolean, promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(context = reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          dataStore.updateCallSettings(hideCall = value, updateCall = false)
          promise.resolve("Secure Number Updated")
        } catch (e: Exception) {
          promise.reject("Secure Failed", "Failed to secure the number: ${e.message}")
        }
      }
    } catch (e: Exception) {
      promise.reject("Secure Failed", "Failed to secure the number: ${e.message}")
    }
  }

  @ReactMethod
  override fun getSecureNumber(promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(context = reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val hideCallValue = dataStore.secureNumberStatus.first()
          promise.resolve(hideCallValue)
        } catch (e: Exception) {
          promise.reject("Fetch Failed", "Failed to get secure number: ${e.message}")
        }
      }
    } catch (e: Exception) {
      promise.reject("Fetch Failed", "Failed to get secure number: ${e.message}")
    }
  }

  @ReactMethod
  override fun toggleVibration(value: Boolean, promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(context = reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          dataStore.updateVibrationSetting(vibrate = value)
          promise.resolve("Vibration setting updated")
        } catch (e: Exception) {
          promise.reject("Vibration Failed", "Failed to update vibration setting: ${e.message}")
        }
      }
    } catch (e: Exception) {
      promise.reject("Vibration Failed", "Failed to update vibration setting: ${e.message}")
    }
  }

  @ReactMethod
  override fun getVibrationStatus(promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(context = reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val vibrationEnabled = dataStore.vibrationStatus.first()
          promise.resolve(vibrationEnabled)
        } catch (e: Exception) {
          promise.reject("Fetch Failed", "Failed to get vibration status: ${e.message}")
        }
      }
    } catch (e: Exception) {
      promise.reject("Fetch Failed", "Failed to get vibration status: ${e.message}")
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  override fun forwardAllCalls(cfi: Boolean, phoneNumber: String, countryCode: String?, subscriptionId: Double, promise: Promise) {
    try {
      // Convert subscriptionId to Int, as it's expected to be an integer ID
      val subscriptionIdInt = subscriptionId.toInt()
      setCallForwarding(reactApplicationContext, cfi, phoneNumber, countryCode, subscriptionIdInt, promise)
    } catch (e: Exception) {
      promise.reject("ERROR", "Failed to forward calls: ${e.localizedMessage}")
    }
  }

  @ReactMethod
  override fun saveReplies(reply: String, promise: Promise) {
    val dataStore = CallSettingDataStore(context = reactApplicationContext)
    CoroutineScope(Dispatchers.IO).launch {
      try {
        dataStore.saveReply(reactApplicationContext, reply)
        withContext(Dispatchers.Main) { promise.resolve("Saved") }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) { promise.reject("SAVE_ERROR", e) }
      }
    }
  }

  @ReactMethod
  override fun updateReplies(replies: ReadableArray, promise: Promise) {
    val dataStore = CallSettingDataStore(context = reactApplicationContext)
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val repliesList = mutableListOf<String>()
        for (i in 0 until replies.size()) {
          repliesList.add(replies.getString(i).toString())
        }
        dataStore.updateReplies(reactApplicationContext, repliesList)
        withContext(Dispatchers.Main) { promise.resolve("Replies Updated") }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) { promise.reject("UPDATE_ERROR", e) }
      }
    }
  }

  @ReactMethod
  override fun deleteReply(reply: String, promise: Promise) {
    val dataStore = CallSettingDataStore(context = reactApplicationContext)
    CoroutineScope(Dispatchers.IO).launch {
      try {
        dataStore.deleteReply(reply)
        withContext(Dispatchers.Main) { promise.resolve("Reply Deleted") }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) { promise.reject("DELETE_ERROR", e) }
      }
    }
  }

  @ReactMethod
  override fun getReplies(promise: Promise) {
    val dataStore = CallSettingDataStore(context = reactApplicationContext)
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val replies = dataStore.repliesFlow.first()
        val result = Arguments.createArray().apply {
          replies.forEach { pushString(it) }
        }
        withContext(Dispatchers.Main) { promise.resolve(result) }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) { promise.reject("GET_ERROR", e) }
      }
    }
  }

  @ReactMethod
  override fun getAllContacts(promise: Promise) {
    try {
      val contacts = SimpleContactsHelper(reactApplicationContext)
      val result = Arguments.createArray()
      contacts.getAvailableContacts(false) { items ->
        items.forEach { contact ->
          val contactMap = Arguments.createMap().apply {
            putInt("rawId", contact.rawId)
            putInt("contactId", contact.contactId)
            putString("name", contact.name)
            putString("photoUri", contact.photoUri)

            val phoneArray = Arguments.createArray()
            contact.phoneNumbers.forEach { number ->
              val numberMap = Arguments.createMap()
              numberMap.putString("value", number.value)
              numberMap.putInt("type", number.type)
              numberMap.putString("label", number.label)
              numberMap.putString("normalizedNumber", number.normalizedNumber)
              numberMap.putBoolean("isPrimary", number.isPrimary)
              phoneArray.pushMap(numberMap)
            }
            putArray("phoneNumbers", phoneArray)

            val birthdaysArray = Arguments.createArray()
            contact.birthdays.forEach { birthdaysArray.pushString(it) }
            putArray("birthdays", birthdaysArray)

            val anniversariesArray = Arguments.createArray()
            contact.anniversaries.forEach { anniversariesArray.pushString(it) }
            putArray("anniversaries", anniversariesArray)
          }
          result.pushMap(contactMap)
        }
      }
      promise.resolve(result)
    } catch (e: Exception) {
      promise.reject("GET_CONTACTS_ERROR", "Failed to fetch contacts: ${e.localizedMessage}", e)
    }
  }

  @ReactMethod
  override fun getContactById(rawContactId: Double, promise: Promise) {
    ensureBackgroundThread {
      try {
        // Convert rawContactId to Long, as ContactHelper expects a Long
        val rawContactIdLong = rawContactId.toLong()
        val contactHelper = ContactHelper(reactApplicationContext)
        val contact = contactHelper.getContactById(rawContactIdLong)

        if (contact != null) {
          val contactMap = Arguments.createMap().apply {
            putInt("rawId", contact.rawId)
            putInt("contactId", contact.contactId)
            putString("name", contact.name)
            putString("photoUri", contact.photoUri)
            putString("prefix", contact.prefix)
            putString("firstName", contact.firstName)
            putString("middleName", contact.middleName)
            putString("surname", contact.surname)
            putString("suffix", contact.suffix)
            putString("nickname", contact.nickname)
            putString("thumbnailUri", contact.thumbnailUri)
            putString("notes", contact.notes)
            putString("source", contact.source)
            putInt("starred", contact.starred)
            putString("mimetype", contact.mimetype)
            putString("ringtone", contact.ringtone)

            val phoneArray = Arguments.createArray()
            contact.phoneNumbers.forEach { number ->
              val numberMap = Arguments.createMap()
              numberMap.putString("value", number.value)
              numberMap.putInt("type", number.type)
              numberMap.putString("label", number.label)
              numberMap.putString("normalizedNumber", number.normalizedNumber)
              numberMap.putBoolean("isPrimary", number.isPrimary)
              phoneArray.pushMap(numberMap)
            }
            putArray("phoneNumbers", phoneArray)

            val emailArray = Arguments.createArray()
            contact.emails.forEach { email ->
              val emailMap = Arguments.createMap()
              emailMap.putString("value", email.value)
              emailMap.putInt("type", email.type)
              emailMap.putString("label", email.label)
              emailArray.pushMap(emailMap)
            }
            putArray("emails", emailArray)

            val addressArray = Arguments.createArray()
            contact.addresses.forEach { address ->
              val addressMap = Arguments.createMap()
              addressMap.putString("value", address.value)
              addressMap.putInt("type", address.type)
              addressMap.putString("label", address.label)
              addressArray.pushMap(addressMap)
            }
            putArray("addresses", addressArray)

            val eventArray = Arguments.createArray()
            contact.events.forEach { event ->
              val eventMap = Arguments.createMap()
              eventMap.putString("value", event.value)
              eventMap.putInt("type", event.type)
              eventArray.pushMap(eventMap)
            }
            putArray("events", eventArray)

            val birthdaysArray = Arguments.createArray()
            contact.birthdays.forEach { birthdaysArray.pushString(it) }
            putArray("birthdays", birthdaysArray)

            val anniversariesArray = Arguments.createArray()
            contact.anniversaries.forEach { anniversariesArray.pushString(it) }
            putArray("anniversaries", anniversariesArray)

            val groupArray = Arguments.createArray()
            contact.groups.forEach { group ->
              val groupMap = Arguments.createMap()
              group.id?.let { groupMap.putInt("id", it.toInt()) }
              groupMap.putString("title", group.title)
              groupArray.pushMap(groupMap)
            }
            putArray("groups", groupArray)

            val orgMap = Arguments.createMap()
            orgMap.putString("company", contact.organization.company)
            orgMap.putString("title", contact.organization.jobPosition)
            putMap("organization", orgMap)

            val websiteArray = Arguments.createArray()
            contact.websites.forEach { websiteArray.pushString(it) }
            putArray("websites", websiteArray)

            val imArray = Arguments.createArray()
            contact.IMs.forEach { im ->
              val imMap = Arguments.createMap()
              imMap.putString("value", im.value)
              imMap.putInt("type", im.type)
              imMap.putString("label", im.label)
              imArray.pushMap(imMap)
            }
            putArray("IMs", imArray)
          }
          promise.resolve(contactMap)
        } else {
          promise.reject("CONTACT_NOT_FOUND", "Contact with ID $rawContactIdLong not found")
        }
      } catch (e: Exception) {
        promise.reject("GET_CONTACT_ERROR", "Failed to fetch contact: ${e.localizedMessage}", e)
      }
    }
  }

  @ReactMethod
  override fun createNewContact(contactMap: ReadableMap, promise: Promise) {
    try {
      val contactHelper = ContactHelper(reactApplicationContext)
      val contact = contactHelper.readableMapToContact(contactMap)
      val success = contactHelper.insertContact(contact)

      if (success) {
        promise.resolve("Contact created successfully")
      } else {
        promise.reject("INSERT_FAILED", "Failed to insert contact")
      }
    } catch (e: Exception) {
      promise.reject("ERROR_CREATING_CONTACT", e.message, e)
    }
  }

  @ReactMethod
  override fun updateContact(contactMap: ReadableMap, photoStatus: Double, promise: Promise) {
    try {
      // Convert photoStatus to Int, as it's expected to be an integer
      val photoStatusInt = photoStatus.toInt()
      val contactHelper = ContactHelper(reactApplicationContext)
      val contact = contactHelper.readableMapToContact(contactMap)
      val success = contactHelper.updateContact(contact, photoStatusInt)

      if (success) {
        promise.resolve("Contact updated successfully")
      } else {
        promise.reject("UPDATE_FAILED", "Failed to update contact")
      }
    } catch (e: Exception) {
      promise.reject("ERROR_UPDATING_CONTACT", e.message, e)
    }
  }

  @ReactMethod
  override fun deleteContact(contact: ReadableMap, promise: Promise) {
    ensureBackgroundThread {
      try {
        val contactHelper = ContactHelper(reactApplicationContext)
        val parsedContact = contactHelper.readableMapToContact(contact)
        contactHelper.deleteContact(parsedContact) { success ->
          if (success) {
            promise.resolve("Contact deleted successfully")
          } else {
            promise.reject("DELETE_FAILED", "Failed to delete contact")
          }
        }
      } catch (e: Exception) {
        promise.reject("UNEXPECTED_ERROR", "An error occurred while deleting contact: ${e.localizedMessage}", e)
      }
    }
  }

  @ReactMethod
  override fun isNumberBlocked(phoneNumber: String, promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val isBlocked = dataStore.isNumberBlocked(phoneNumber)
          withContext(Dispatchers.Main) { promise.resolve(isBlocked) }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) { promise.reject("BLOCK_CHECK_ERROR", "Failed to check if number is blocked: ${e.message}") }
        }
      }
    } catch (e: Exception) {
      promise.reject("BLOCK_CHECK_ERROR", "Failed to check if number is blocked: ${e.message}")
    }
  }

  @ReactMethod
  override fun getBlockedNumbers(promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dataStore = CallSettingDataStore(reactApplicationContext.applicationContext)
        val blocked = dataStore.blockedNumbers.first()
        promise.resolve(Arguments.fromList(blocked.toList()))
      } catch (e: Exception) {
        promise.reject("GET_BLOCKED_FAILED", e)
      }
    }
  }

  @ReactMethod
  override fun addBlockedNumber(number: String, promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dataStore = CallSettingDataStore(reactApplicationContext)
        dataStore.addBlockedNumber(number)
        promise.resolve("Number $number successfully added to block list.")
      } catch (e: Exception) {
        promise.reject("ADD_BLOCKED_FAILED", e)
      }
    }
  }

  @ReactMethod
  override fun removeBlockedNumber(number: String, promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dataStore = CallSettingDataStore(reactApplicationContext)
        dataStore.removeBlockedNumber(number)
        promise.resolve("Number $number successfully removed from block list.")
      } catch (e: Exception) {
        promise.reject("REMOVE_BLOCKED_FAILED", e)
      }
    }
  }

  @ReactMethod
  override fun toggleShowBlockNotification(promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dataStore = CallSettingDataStore(reactApplicationContext)
        val current = dataStore.showBlockNotificationStatus
        dataStore.toggleShowBlockNotification()
        promise.resolve("Show block notification toggled to ${!current.first()}")
      } catch (e: Exception) {
        promise.reject("TOGGLE_NOTIFICATION_FAILED", e)
      }
    }
  }

  @ReactMethod
  override fun getBlockNotificationStatus(promise: Promise) {
    try {
      val dataStore = CallSettingDataStore(reactApplicationContext)
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val status = dataStore.showBlockNotificationStatus.first()
          withContext(Dispatchers.Main) { promise.resolve(status) }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            promise.reject("FETCH_NOTIFICATION_STATUS_FAILED", "Failed to get block notification status: ${e.message}")
          }
        }
      }
    } catch (e: Exception) {
      promise.reject("FETCH_NOTIFICATION_STATUS_FAILED", "Failed to get block notification status: ${e.message}")
    }
  }

  @RequiresPermission(Manifest.permission.READ_CALL_LOG)
  @ReactMethod
  override fun getCallLogs(promise: Promise) {
    ensureBackgroundThread {
      try {
        val result = Arguments.createArray()
        val cursor = reactApplicationContext.contentResolver.query(
          CallLog.Calls.CONTENT_URI,
          arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME
          ),
          null,
          null,
          "${CallLog.Calls.DATE} DESC LIMIT 500" // limit to last 500 to prevent OOM
        )

        cursor?.use {
          if (it.moveToFirst()) {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)

            if (numberIndex != -1 && typeIndex != -1 && dateIndex != -1) {
              do {
                val number = it.getString(numberIndex) ?: ""
                val callType = it.getInt(typeIndex)
                val callDate = it.getLong(dateIndex)
                val duration = it.getLong(durationIndex)
                val name = if (nameIndex != -1) it.getString(nameIndex) ?: "" else ""

                val map = Arguments.createMap()
                map.putString("number", number)
                map.putInt("type", callType)
                map.putDouble("date", callDate.toDouble())
                map.putDouble("duration", duration.toDouble())
                map.putString("name", name)
                
                result.pushMap(map)
              } while (it.moveToNext())
            }
          }
        }
        promise.resolve(result)
      } catch (e: Exception) {
        promise.reject("GET_CALLLOGS_ERROR", e.localizedMessage, e)
      }
    }
  }

  @ReactMethod
  override fun getDefaultDialerPackage(promise: Promise) {
    try {
      val telecomManager = reactApplicationContext.getSystemService(TelecomManager::class.java)
      if (telecomManager != null) {
        promise.resolve(telecomManager.defaultDialerPackage ?: "")
      } else {
        promise.reject("TELECOM_MANAGER_ERROR", "TelecomManager not available")
      }
    } catch (e: Exception) {
      promise.reject("GET_DEFAULT_DIALER_ERROR", e.localizedMessage, e)
    }
  }

  @ReactMethod
  override fun checkIfDefaultDialer(promise: Promise) {
    try {
      val telecomManager = reactApplicationContext.getSystemService(TelecomManager::class.java)
      if (telecomManager != null) {
        val defaultDialer = telecomManager.defaultDialerPackage
        val packageName = reactApplicationContext.packageName
        promise.resolve(defaultDialer == packageName)
      } else {
        promise.reject("TELECOM_MANAGER_ERROR", "TelecomManager not available")
      }
    } catch (e: Exception) {
      promise.reject("CHECK_DEFAULT_DIALER_ERROR", e.localizedMessage, e)
    }
  }

  @ReactMethod
  override fun openDialerSetting(promise: Promise) {
    try {
      val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (intent.resolveActivity(reactApplicationContext.packageManager) != null) {
        reactApplicationContext.startActivity(intent)
        promise.resolve("Opened Settings")
      } else {
        promise.reject("INTENT_ERROR", "Action not supported on this device")
      }
    } catch (e: Exception) {
      promise.reject("OPEN_SETTING_ERROR", e.localizedMessage, e)
    }
  }

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = "Dialpad"
  }
}
