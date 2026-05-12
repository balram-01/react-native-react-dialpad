package com.dialpad.callManager

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.dialpad.models.AudioRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class CallViewModel(private val callRepo: CallRepository,
                    private val audioManager: AudioManager):ViewModel()
{
  private val _call = MutableStateFlow(callRepo.getCall());
  val phoneState: StateFlow<PhoneState> = callRepo.phoneState
  private val TAG = "CallViewModel"
  val call: StateFlow<Call?>
    get() = _call


  private val _callState = MutableStateFlow(_call.value?.state!!)
  val callState: StateFlow<Int>
    get() = _callState

  private val _isSpeakerOn = MutableStateFlow(false)
  val isSpeakerOn: StateFlow<Boolean> get() = _isSpeakerOn

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> get() = _isMuted

  private val _isHold = MutableStateFlow(false)
  val isHold: StateFlow<Boolean> get() = _isHold

  val currentAudioState :StateFlow<CallAudioState?> = (callRepo.audioState)

  private val callback = object : Call.Callback() {
    override fun onStateChanged(call: Call, newState: Int) {
      _callState.value = newState
      //Log.d("call view model",newState.toString())
    }
    override fun onDetailsChanged(call: Call, details: Call.Details) {
      //Log.d("call view model","deatail chmhe")
    }

    override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: MutableList<Call>) {
      Log.d("view model conference",conferenceableCalls.size.toString())
    }
  }

  init {
    call.value?.registerCallback(callback)
    setAudioModeForCall()
  }

  fun toggleSpeaker() {
    val currentRoute = currentAudioState.value?.route ?: CallAudioState.ROUTE_EARPIECE
    val isBluetoothAvailable = currentAudioState.value?.supportedBluetoothDevices?.isNotEmpty() == true
    
    when (currentRoute) {
      CallAudioState.ROUTE_EARPIECE -> {
        callRepo.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        _isSpeakerOn.value = true
      }
      CallAudioState.ROUTE_SPEAKER -> {
        if (isBluetoothAvailable) {
          callRepo.setAudioRoute(CallAudioState.ROUTE_BLUETOOTH)
          _isSpeakerOn.value = true
        } else {
          callRepo.setAudioRoute(CallAudioState.ROUTE_EARPIECE)
          _isSpeakerOn.value = false
        }
      }
      CallAudioState.ROUTE_BLUETOOTH -> {
        callRepo.setAudioRoute(CallAudioState.ROUTE_EARPIECE)
        _isSpeakerOn.value = false
      }
      else -> {
        callRepo.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        _isSpeakerOn.value = true
      }
    }
  }

  fun toogleHold(){
    _isHold.value = callRepo.toggleHold()
  }

  fun toggleSwap(){
    callRepo.swap()
  }

  fun toggleMerge(){
    callRepo.merge()
  }

  fun toggleMute() {
    val newMuteState = !_isMuted.value
    callRepo.setMute(newMuteState)
    audioManager.isMicrophoneMute = newMuteState
    _isMuted.value = newMuteState
  }


  fun answerCall() {
    _call.value?.answer(VideoProfile.STATE_AUDIO_ONLY)
  }

  fun rejectCall() {
    callRepo.getPrimaryCall()?.disconnect()
    resetAudioMode()
  }

  fun playDtmfTone(char: Char) {
    callRepo.getPrimaryCall()?.playDtmfTone(char)
  }

  fun stopDtmfTone() {
    callRepo.getPrimaryCall()?.stopDtmfTone()
  }

  private fun setAudioModeForCall() {
    audioManager.mode = AudioManager.MODE_IN_CALL
    audioManager.isMicrophoneMute = false
    try {
      callRepo.setMute(false)
    } catch (e: Exception) {
      Log.e(TAG, "Error unmuting: ${e.message}")
    }
  }

  private fun resetAudioMode() {
    audioManager.mode = AudioManager.MODE_NORMAL
    audioManager.isMicrophoneMute = false
  }

  override fun onCleared() {
    super.onCleared()
    resetAudioMode()
    _call.value?.unregisterCallback(callback)
  }

}
