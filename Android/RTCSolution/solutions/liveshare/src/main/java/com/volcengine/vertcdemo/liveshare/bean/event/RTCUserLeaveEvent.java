// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.bean.event;

public class RTCUserLeaveEvent {
    public String userId;

    public RTCUserLeaveEvent(String userId) {
        this.userId = userId;
    }
}
