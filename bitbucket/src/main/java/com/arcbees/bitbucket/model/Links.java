/**
 * Copyright 2014 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.arcbees.bitbucket.model;

public class Links {
    private Link decline;
    private Link commits;
    private Link self;
    private Link accept;
    private Link html;
    private Link comments;
    private Link activity;
    private Link diff;
    private Link approvals;

    public Link getDecline() {
        return decline;
    }

    public void setDecline(Link decline) {
        this.decline = decline;
    }

    public Link getCommits() {
        return commits;
    }

    public void setCommits(Link commits) {
        this.commits = commits;
    }

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getAccept() {
        return accept;
    }

    public void setAccept(Link accept) {
        this.accept = accept;
    }

    public Link getHtml() {
        return html;
    }

    public void setHtml(Link html) {
        this.html = html;
    }

    public Link getComments() {
        return comments;
    }

    public void setComments(Link comments) {
        this.comments = comments;
    }

    public Link getActivity() {
        return activity;
    }

    public void setActivity(Link activity) {
        this.activity = activity;
    }

    public Link getDiff() {
        return diff;
    }

    public void setDiff(Link diff) {
        this.diff = diff;
    }

    public Link getApprovals() {
        return approvals;
    }

    public void setApprovals(Link approvals) {
        this.approvals = approvals;
    }
}
