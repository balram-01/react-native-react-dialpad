package com.dialpad.callManager

import android.content.Context
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.Call
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.dialpad.CallSettingDataStore
import com.dialpad.R
import com.dialpad.extensions.getStateCompat
import com.dialpad.extensions.isConference
import com.dialpad.extensions.*
import com.dialpad.helpers.*
import com.dialpad.models.CallContact
import kotlinx.coroutines.flow.first

@Composable
fun MainCallHandler(
  callViewModel: CallViewModel,
  onReject: () -> Unit,
  onAddNewCall:() ->Unit
){
  val currCallState by callViewModel.callState.collectAsState()
  val currCall by callViewModel.call.collectAsState()
  val isMuted by callViewModel.isMuted.collectAsState()
  val isSpeakerOn by callViewModel.isSpeakerOn.collectAsState()
  val isHold by callViewModel.isHold.collectAsState()
  val callerNumber = currCall?.details?.handle?.schemeSpecificPart ?: "Unknown"
  val callState by callViewModel.phoneState.collectAsState()
  val callAudioState by callViewModel.currentAudioState.collectAsState()

  val context = LocalContext.current

  var proximityWakeLock by remember { mutableStateOf<PowerManager.WakeLock?>(null) }
  var screenOnWakeLock by remember { mutableStateOf<PowerManager.WakeLock?>(null) }

  fun enableProximitySensor() {
    if ((proximityWakeLock == null || proximityWakeLock?.isHeld == false)) {
      val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
      proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "com.rndialpad:wake_lock")
      proximityWakeLock!!.acquire(60 * 60 * 1000L)
    }
  }

  fun disableProximitySensor() {
    if (proximityWakeLock?.isHeld == true) {
      proximityWakeLock!!.release()
    }
  }

  LaunchedEffect(Unit) {
    enableProximitySensor()
  }

  DisposableEffect(Unit) {
    onDispose {
      disableProximitySensor()
    }
  }

  when(callState){
    is NoCall->onReject()
    is SingleCall ->{
      val state = (callState as SingleCall).call.getStateCompat()
      val activeCall = (callState as SingleCall).call
      if(state == Call.STATE_NEW || state == Call.STATE_RINGING){
        IncomingCallScreen(callerNumber = callerNumber,
          call=activeCall,
          state = currCallState, onAnswer = {callViewModel.answerCall()},
          onReject = { onReject() })
      }
      else{
        OnGoingCallScreen(onReject = { onReject()},
          callerNumber = callerNumber,
          state = currCallState,
          isSpeakerOn = isSpeakerOn,
          isMuted = isMuted,
          isHold = isHold,
          toggleSpeaker = {callViewModel.toggleSpeaker()},
          toggleMute = {callViewModel.toggleMute()},
          onAddCall = onAddNewCall,
          callState = callState,
          activeCall = activeCall,
          toggleHold = {callViewModel.toogleHold()},
          toggleSwap = { callViewModel.toggleSwap()},
          toggleMerge = {callViewModel.toggleMerge()},
          callAudioState= callAudioState
        )
      }
    }
    is TwoCalls ->{
      val activeCall = (callState as TwoCalls).active
      val onHoldCall = (callState as TwoCalls).onHold

      OnGoingCallScreen(onReject = { onReject()},
        callerNumber = callerNumber,
        state = currCallState,
        isSpeakerOn = isSpeakerOn,
        isMuted = isMuted,
        isHold = isHold,
        toggleSpeaker = {callViewModel.toggleSpeaker()},
        toggleMute = {callViewModel.toggleMute()},
        onAddCall = onAddNewCall,
        callState = callState,
        showHoldUI= true,
        activeCall = activeCall,
        holdCall = onHoldCall,
        toggleHold = {callViewModel.toogleHold()},
        toggleSwap = { callViewModel.toggleSwap()},
        toggleMerge = {callViewModel.toggleMerge()},
        callAudioState= callAudioState
      )
    }
  }

}

@Composable
fun IncomingCallScreen(
  callerNumber: String,
  call:Call?,
  state:Int,
  onAnswer: () -> Unit,
  onReject: () -> Unit,
  modifier:Modifier = Modifier
) {
  val height = LocalConfiguration.current.screenHeightDp
  val width = LocalConfiguration.current.screenWidthDp
  var currCallInfo by remember { mutableStateOf<CallContact?>(null) }
  val context = LocalContext.current
  var vibrator by remember { mutableStateOf<Vibrator?>(null) }

  fun startVibration() {
    vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator?.hasVibrator() == true) {
      if(isOreoPlus()){
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
      }
      else{
        vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
      }
    }
  }

  fun stopVibration() {
    vibrator?.cancel()
  }

  LaunchedEffect(Unit) {
    val isVibrationEnabled = CallSettingDataStore(context)

    if(isVibrationEnabled.vibrationStatus.first()){
      startVibration()
    }
  }

  LaunchedEffect(callerNumber){
    getCallContact(context,call!!,callerNumber,{currCallInfo = it})
  }

  DisposableEffect(Unit) {
    onDispose {
      stopVibration()
    }
  }

  fun sendDirectMessage(msg:String){
    context.sendDirectMessage(callerNumber,msg)
    onReject()
  }

  Box(modifier = modifier.fillMaxSize().background(Color(0xFF121212))) {
    Column(modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(80.dp))
      
      // Top Info
      Column(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        Text(text = state.asString() + "...",
          fontSize = 18.sp,
          fontFamily = FontFamily.Default,
          fontWeight = FontWeight.Medium,
          color = Color.LightGray
        )
        Box(modifier = modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(Color(0xFF2C2C2E))
        ){
          val imageUri = currCallInfo?.photoUri.takeIf { !it.isNullOrEmpty() }
            ?: R.drawable.baseline_person_24 // Fallback to the drawable resource

          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(imageUri)
              .transformations(CircleCropTransformation())
              .build(),
            contentDescription = "Caller Avatar",
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.baseline_person_24),
            error = painterResource(R.drawable.baseline_person_24),
            contentScale = ContentScale.Crop
          )
        }
        Text(text=currCallInfo?.name.takeIf { !it.isNullOrBlank() } ?: callerNumber,
          fontSize = 34.sp,
          fontFamily = FontFamily.Default,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      // Bottom Actions
      Row(modifier = modifier.fillMaxWidth()
        .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ){

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFFF3B30))
            .size(76.dp)
            .clickable { onReject() },
            contentAlignment = Alignment.Center
          ){
            Image(painter = painterResource(R.drawable.baseline_call_end_24),
              contentDescription = "Decline",
              modifier = Modifier.size(36.dp),
              colorFilter =  ColorFilter.tint(Color.White),
              contentScale = ContentScale.Crop)
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text("Decline", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Box(modifier = modifier
          .clip(CircleShape)
          .background(Color(0xFF3A3A3C))
          .padding(14.dp)
        ){
          var expanded by remember { mutableStateOf(false) }

          Image(
            painter = painterResource(R.drawable.baseline_chat_bubble_24),
            contentDescription = "Message",
            modifier = Modifier.size(24.dp)
              .clickable { expanded = !expanded },
            colorFilter =  ColorFilter.tint(Color.White),
            contentScale = ContentScale.Crop
          )

          QuickReplyDropdownMenu(
            onSendMessage = {
              sendDirectMessage(it)
              expanded = false
            },
            expanded=expanded,
            onDismiss = {expanded = !expanded}
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF34C759))
            .size(76.dp)
            .clickable{ onAnswer() },
            contentAlignment = Alignment.Center
          ){
            Image(painter = painterResource(R.drawable.baseline_call_24),
              contentDescription = "Answer",
              modifier = Modifier.size(36.dp),
              colorFilter =  ColorFilter.tint(Color.White),
              contentScale = ContentScale.Crop)
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text("Answer", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

      }

    }
  }
}

@Composable
fun QuickReplyDropdownMenu(
  onSendMessage: (String) -> Unit,
  expanded:Boolean,
  onDismiss :()->Unit,
) {

  var showCustomDialog by remember { mutableStateOf(false) }
  var customMessage by remember { mutableStateOf("") }
  val context = LocalContext.current
  val dataStore = remember { CallSettingDataStore(context) }
  val options by dataStore.repliesFlow.collectAsState(initial = listOf("Loading..."))
  Box {
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismiss,
      offset = DpOffset(
        10.dp,10.dp
      )
    ) {
      options.forEach { message ->
        DropdownMenuItem(
          text = { Text(
            text = message,
            fontSize = 12.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            color = Color.Black
          ) },
          onClick = {
            if (message == "Custom") {
              showCustomDialog = true
              onDismiss()
            } else {
              onSendMessage(message)
            }
          },
          leadingIcon = {
            Image(
              painter = painterResource(R.drawable.baseline_chat_bubble_24),
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          }
        )
      }
    }
  }

  if (showCustomDialog) {
    AlertDialog(
      onDismissRequest = {
        showCustomDialog = false
        customMessage = ""
      },
      title = { Text(text = "Enter Custom Message...") },
      text = {
        TextField(
          value = customMessage,
          onValueChange = { customMessage = it },
          label = { Text("Message") }
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onSendMessage(customMessage)
            showCustomDialog = false
            customMessage = ""
          }
        ) {
          Text("Send")
        }
      },
      dismissButton = {
        Button(
          onClick = {
            showCustomDialog = false
            customMessage = ""
          }
        ) {
          Text("Cancel")
        }
      }
    )
  }
}


@Preview(showBackground = true)
@Composable
fun InComingCallScreenPreview(){
  //IncomingCallScreen(callerNumber = "+919117517898", onAnswer = {}, onReject = {}, state = 1)
}



