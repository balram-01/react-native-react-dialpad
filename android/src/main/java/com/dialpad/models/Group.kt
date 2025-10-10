package com.dialpad.models

import com.dialpad.helpers.FIRST_GROUP_ID
import java.io.Serializable


@kotlinx.serialization.Serializable
data class Group(
  var id: Long?,
  var title: String,
  var contactsCount: Int = 0
) : Serializable {

  fun addContact() = contactsCount++

  fun getBubbleText() = title

  fun isPrivateSecretGroup() = (id ?: 0) >= FIRST_GROUP_ID
}
