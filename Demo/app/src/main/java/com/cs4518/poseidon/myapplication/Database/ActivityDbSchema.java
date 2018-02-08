package com.cs4518.poseidon.myapplication.Database;

/**
 * Created by Poseidon on 2/6/18.
 */

public class ActivityDbSchema {
    public static final class ActivityTable {
        public static final String NAME = "activities";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String ACTIVITY = "activity";
            public static final String START_TIME = "start_time";
        }
    }
}
