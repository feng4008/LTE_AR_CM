package com.hwacom.cm.model;

public enum CommandEnum {
    
    LAR_BACKUP {

        @Override
        public String getCommand() {
            //show running-config | file disk[d]:[admin-]running-config-[YYYY-MM-DD].cfg location 0/RSP0/CPU0
            return "%1$sshow running-config | file disk%2$d:%3$srunning-config-%4$tY-%4$tm-%4$te.cfg%5$s";
        }

        @Override
        public RunningConfigTypeEmum getRunningConfigTypeEmum() {
            return RunningConfigTypeEmum.LAR_BACKUP;
        }
        
    },
    LAR_DEL {

        @Override
        public String getCommand() {
            //delete disk[d]:[admin-]running-config-[YYYY-MM-DD].cfg location all
            return "delete disk%1$d:%2$srunning-config-%3$tY-%3$tm-%3$te.cfg%4$s";
        }

        @Override
        public RunningConfigTypeEmum getRunningConfigTypeEmum() {
            return RunningConfigTypeEmum.LAR_DEL;
        }
        
    },
	CENTRE_BACKUP {

		@Override
		public String getCommand() {
		    //scp disk1:/[admin-]running-config-[YYYY-MM-DD].cfg [location 0/RSP0/CPU0] [login]@[ip]:[path][admin-]running-config-[YYYY-MM-DD].cfg [vrf OAM]
		    return "scp disk%1$d:/%2$srunning-config-%3$tY-%3$tm-%3$te.cfg %4$s %5$s@%6$s:%7$s%2$srunning-config-%3$tY-%3$tm-%3$te.cfg %8$s";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.CENTRE_BACKUP;
		}
		
	},
	CENTRE_DEL {

		@Override
		public String getCommand() {
		    
            return "%1$s%2$srunning-config-%3$tY-%3$tm-%3$te.cfg";
		}

		@Override
		public RunningConfigTypeEmum getRunningConfigTypeEmum() {
			return RunningConfigTypeEmum.CENTRE_DEL;
		}
		
	};
	
	public abstract String getCommand();
	
	public abstract RunningConfigTypeEmum getRunningConfigTypeEmum();
}
