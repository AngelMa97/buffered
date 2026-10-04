package com.angelma.mappers

import com.angelma.models.License
import com.angelma.records.LicenseRecord

fun LicenseRecord.toLicense() = License(
    name = name,
    url = url
)
