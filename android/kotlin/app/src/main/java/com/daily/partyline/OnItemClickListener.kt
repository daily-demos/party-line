package com.daily.partyline

interface OnItemClickListener {
    open fun onModeratorMute(Id: String?)
    open fun onModeratorChangeRole(Id: String?)
    open fun onModeratorMakeModerator(Id: String?)
    open fun onModeratorEject(Id: String?)
}