// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.bean.event;

public class UserLeaveEvent {
    public String userId;

    public UserLeaveEvent(String userId) {
        this.userId = userId;
    }
}
