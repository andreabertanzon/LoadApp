package com.abcode.loadapp

sealed class BtnState {
    object Loading: BtnState()
    object Done: BtnState()
}