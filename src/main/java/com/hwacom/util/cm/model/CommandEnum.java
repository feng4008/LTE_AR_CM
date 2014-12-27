package com.hwacom.util.cm.model;

public enum CommandEnum {
	
	RUNNING_SHOW {

		@Override
		public String getCommand() {
//			return "show running-config";
		    return "show running-config | file ftp://";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.RUNNING_SAVE;
		}
		
	},
	ADMIN_SHOW {

		@Override
		public String getCommand() {
//			return "admin show running-config";
            return "admin show running-config | file ftp://";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.ADMIN_SAVE;
		}
		
	},
	RUNNING_LOCAL_BACKUP {

		@Override
		public String getCommand() {
			return "show running-config | file disk1:running-config";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.RUNNING_LOCAL_BACKUP;
		}
		
	},
	ADMIN_LOCAL_BACKUP {

		@Override
		public String getCommand() {
			return "admin show running-config | file disk1:admin-running-config";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.ADMIN_LOCAL_BACKUP;
		}
		
	};
	
	public abstract String getCommand();
	
	public abstract RunningConfigTypeEmum getRunningConfigTypeEmum();
}
