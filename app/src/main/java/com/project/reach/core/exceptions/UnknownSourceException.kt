package com.project.reach.core.exceptions

class UnknownSourceException: Exception() {
    override fun toString(): String {
        return "Received packet from an unknown source"
    }
}