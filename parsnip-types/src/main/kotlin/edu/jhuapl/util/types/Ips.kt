/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Ips.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2024 Johns Hopkins University Applied Physics Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.jhuapl.util.types

import org.apache.commons.net.util.SubnetUtils
import java.util.regex.Pattern


internal const val IPV4_ADDRESS = "\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"

/** Pattern for an IPv4 address. */
val IPV4_PATTERN: Pattern = Pattern.compile(IPV4_ADDRESS)

/**
 * Test whether argument is IPv4 address.
 * @return true if IPv4
 */
fun String.isIpv4() = IPV4_PATTERN.matcher(this).matches()

/**
 * Test whether argument is a valid CIDR.
 * @return true if CIDR
 */
fun String.isCidr() = try {
    SubnetUtils(this)
    true
} catch (x: IllegalArgumentException) {
    false
}

/**
 * Test whether an IP is in a CIDR range.
 * @param ip IP to test
 * @param inclusive whether CIDR test should be inclusive (containing .0 and .255)
 * @return true if cidr contains IP
 */
fun String.cidrContains(ip: String, inclusive: Boolean = true) = inclusiveCidr(this, inclusive).isInRange(ip)

/**
 * Creates an inclusive host CIDR info object
 * @param cidr cidr address
 * @param inclusive whether CIDR test should be inclusive (containing .0 and .255)
 * @return subnet
 * @throws IllegalArgumentException if argument is not a valid CIDR string
 */
private fun inclusiveCidr(cidr: String, inclusive: Boolean = true) = with(SubnetUtils(cidr)) {
    isInclusiveHostCount = inclusive
    info
}

//region IP CONVERSIONS

/** Converts IP string to 32-bit unsigned int. Throws [NumberFormatException] if invalid input. */
fun ipToInt(ip: String): Int {
    val sh: ShortArray = ipToArray(ip)
    return (sh[0].toInt() shl 24) + (sh[1].toInt() shl 16) + (sh[2].toInt() shl 8) + sh[3].toInt()
}

/** Converts a 32-bit IP int to String. */
fun ipFromInt(ip: Int): String? {
    val ip1 = ip ushr 24
    val ip2 = (ip ushr 16) - (ip1 shl 8)
    val ip3 = (ip ushr 8) - (ip1 shl 16) - (ip2 shl 8)
    val ip4 = ip - (ip1 shl 24) - (ip2 shl 16) - (ip3 shl 8)
    return String.format("%s.%s.%s.%s", ip1, ip2, ip3, ip4)
}

/** Converts IP string to short[]. */
private fun ipToArray(ip: String): ShortArray {
    val spl = ip.split(".").toTypedArray()
    return shortArrayOf(spl[0].toShort(), spl[1].toShort(), spl[2].toShort(), spl[3].toShort())
}

//endregion
