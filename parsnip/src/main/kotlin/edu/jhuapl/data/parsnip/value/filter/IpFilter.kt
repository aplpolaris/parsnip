package edu.jhuapl.data.parsnip.value.filter

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.cidrContains
import edu.jhuapl.util.types.isCidr
import edu.jhuapl.util.types.isIpv4
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * IpFilter.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2025 Johns Hopkins University Applied Physics Laboratory
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

/**
 * A filter implementation that tests inputs based on a single arbitrary value.
 */
abstract class IpFilter(var value: Any?) : ValueFilter, SimpleValue {

    override val simpleValue
        get() = value

    protected val valueString
        get() = value.toString()

}

/** Tests if an input contains an IPv4 value string. */
object IsIP : ValueFilter {
    override fun invoke(ip: Any?) = ip is String && ip.isIpv4()
}

/** Tests if an input is a CIDR value string. */
object IsCidr : ValueFilter {
    override fun invoke(cidr: Any?) = cidr is String && cidr.isCidr()
}

/** Tests if an input IP and CIDR Pair is true where the ip exists in CIDR range. */
class IpContainedIn @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(cidr: String) : IpFilter(cidr) {
    init {
        require(cidr.isCidr()) { "Invalid CIDR: $cidr" }
    }
    override fun invoke(ip: Any?) = ip is String && IsIP(ip) && valueString.cidrContains(ip)
}

class CidrContains @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(ip: String) : IpFilter(ip) {
    init {
        require(ip.isIpv4()) { "Invalid IP: $ip" }
    }
    override fun invoke(cidr: Any?) = cidr is String && IsCidr(cidr) && cidr.cidrContains(valueString)
}
