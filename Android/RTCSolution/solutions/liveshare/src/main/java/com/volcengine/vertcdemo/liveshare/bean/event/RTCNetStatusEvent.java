// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.bean.event;

public class RTCNetStatusEvent {
    public boolean unblocked;

    public RTCNetStatusEvent(boolean unblocked) {
        this.unblocked = unblocked;
    }
}
