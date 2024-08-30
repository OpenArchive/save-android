package net.opendasharchive.openarchive.features.main

class Rusty {
    external fun rust_greeting(to: String): String

    companion object {
        init {
            System.loadLibrary("hello")
        }
    }
}