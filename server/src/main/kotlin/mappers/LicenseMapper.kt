package com.angelma.mappers

import com.angelma.dtos.LicenseDto
import com.angelma.models.License
import com.angelma.records.LicenseRecord

fun LicenseRecord.toLicense() = License(
    name = name,
    url = url
)

fun License.toLicenseDto() = LicenseDto(
    name = name,
    url = url
)