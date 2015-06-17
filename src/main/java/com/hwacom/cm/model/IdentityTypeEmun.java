package com.hwacom.cm.model;

public enum IdentityTypeEmun {

    ROMC {
        public int getType() {
            return 0;
        }
    },
    LAR {
        public int getType() {
            return 1;
        }
    },
    DEV {
        public int getType() {
            return 2;
        }
    }
}
