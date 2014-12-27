package com.hwacom.util.cm.model;

public enum RunningConfigTypeEmum {
	
	RUNNING_SAVE {
		public int getType() {
			return 1;
		}
	},
	ADMIN_SAVE {
		public int getType() {
			return 0;
		}
	},
	RUNNING_LOCAL_BACKUP {
		public int getType() {
			return 3;
		}
	},
	ADMIN_LOCAL_BACKUP {
		public int getType() {
			return 2;
		}
	};
	
	public abstract int getType();
}
