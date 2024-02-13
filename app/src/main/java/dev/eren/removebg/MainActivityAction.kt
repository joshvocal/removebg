package dev.eren.removebg

interface Action

sealed class MainActivityAction : Action {
    object SegmentImage : MainActivityAction()
    object ChangeBackground : MainActivityAction()
    object PickMedia : MainActivityAction()
}