package constants;

public class JobConstants {

    public static final boolean enableJobs = true;
    public static final int jobOrder = 1;

    public enum LoginJob {
        Resistance(0, JobFlag.ENABLED),
        Adventurer(1, JobFlag.ENABLED),
        Cygnus(2, JobFlag.ENABLED),
        Aran(3, JobFlag.ENABLED),
        Evan(4, JobFlag.ENABLED),
        Mercedes(5, JobFlag.ENABLED),
        Demon(6, JobFlag.ENABLED),
        Phantom(7, JobFlag.ENABLED),
        DualBlade(8, JobFlag.ENABLED),
        Mihile(9, JobFlag.ENABLED),
        Luminous(10, JobFlag.ENABLED),
        Kaiser(11, JobFlag.ENABLED),
        AngelicBuster(12, JobFlag.ENABLED),
        Cannoneer(13, JobFlag.ENABLED),
        Xenon(14, JobFlag.ENABLED),
        Jett(15, JobFlag.DISABLED),
        //Zero(16, JobFlag.DISABLED),
        //Hayato(17, JobFlag.DISABLED),
        //Kanna(18, JobFlag.DISABLED),
        //BeastTamer(19, JobFlag.DISABLED),
        ;
        private final int jobType, flag;

        private LoginJob(int jobType, JobFlag flag) {
            this.jobType = jobType;
            this.flag = flag.getFlag();
        }

        public int getJobType() {
            return jobType;
        }

        public int getFlag() {
            return flag;
        }

        public enum JobFlag {

            DISABLED(0),
            ENABLED(1);
            private final int flag;

            private JobFlag(int flag) {
                this.flag = flag;
            }

            public int getFlag() {
                return flag;
            }
        }
    }

    private JobConstants() {
    }
}
