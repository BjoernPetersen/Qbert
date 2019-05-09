package net.bjoernpetersen.qbert.rest.model

enum class PlayerStateAction {
    PLAY, PAUSE, SKIP
}

data class PlayerStateChange(val action: PlayerStateAction)
