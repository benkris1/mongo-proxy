package com.maxleap.bifrost.kotlin.core.model

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 5:29 PM
 * Email:benkris1@126.com
 *
 */

enum class NamespaceStatus {
  /**
   * Not exists.
   */
  NONE,

  /**
   * Status normally
   */
  ENABLE,

  /**
   * Disable, many reasons.
   */
  DISABLE,

  /**
   * DataSource had deleted
   */
  DELETE,

  /**
   * Just readonly, can't write, delete, update.
   */
  READONLY,

  /**
   * Many reasons, data server is down, upgrade etc.
   */
  MAINTAIN
}
