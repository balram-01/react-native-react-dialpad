package com.dialpad.extensions

import android.telephony.PhoneNumberUtils
import com.dialpad.helpers.normalizeRegex
import java.text.Normalizer

fun String.normalizePhoneNumber() = PhoneNumberUtils.normalizeNumber(this)

fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")
