package com.hwacom.cm.model;

public enum RunningConfigTypeEmum {
	
	LAR_BACKUP {
		public int getType() {
			return 0;
		}
	},
	LAR_DEL {
		public int getType() {
			return 1;
		}
	},
    CENTRE_BACKUP {
        public int getType() {
            return 2;
        }
    },
    CENTRE_DEL {
        public int getType() {
            return 3;
        }
    };
	
	public abstract int getType();
}
