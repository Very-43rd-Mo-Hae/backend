package com.very.relink.group.adapter.out.persistence;

public interface MemberGroupSummaryProjection {

    Long getGroupId();

    String getName();

    long getMemberCount();
}
