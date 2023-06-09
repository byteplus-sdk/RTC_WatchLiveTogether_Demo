// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.feature.player.playerevent;

public class PlayStateExitFullScreen implements PlayerEvent {

    @Override
    public int code() {
        return Action.EXIT_FULL_SCREEN;
    }
}
