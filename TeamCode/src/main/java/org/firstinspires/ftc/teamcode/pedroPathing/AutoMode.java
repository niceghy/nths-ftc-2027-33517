package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "AutoMode", group = "Examples")
public class AutoMode extends OpMode {
    private Follower follower;
    private Timer pathTimer, opmodeTimer;
    private int pathState;

    private SharedPoseStorage.Team selectedTeam = SharedPoseStorage.Team.RED;
    private boolean teamSelected = false;
    private int mode = 1;

    private int startPosition = 0;

    private Pose startPose, passivePose;

    private PathChain toPassive;

    private void setPosesForTeam() {
        if (selectedTeam == SharedPoseStorage.Team.RED) {
            startPose = new Pose(0, 0, Math.toRadians(0));
        } else {
            startPose = new Pose(0, 0, Math.toRadians(0));
        }

        switch (startPosition) {
            case 0:
                startPose = new Pose(22.25, 125, Math.toRadians(140));
                break;
            case 1:
                startPose = new Pose(121.75, 125, Math.toRadians(36));
                break;
            case 2:
                startPose = new Pose(56.75, 8.5, Math.toRadians(90));
                break;
            default:
                startPose = new Pose(87.25, 8.5, Math.toRadians(90));
                break;
        }

        if (mode == 0) {
            if (startPosition == 0 || startPosition == 1) {
                if (selectedTeam == SharedPoseStorage.Team.BLUE) {
                    passivePose = new Pose(60, 132, Math.toRadians(0));
                } else {
                    passivePose = new Pose(84, 132, Math.toRadians(180));
                }
            } else {
                if (selectedTeam == SharedPoseStorage.Team.BLUE) {
                    passivePose = new Pose(36, 12, Math.toRadians(90));
                } else {
                    passivePose = new Pose(108, 12, Math.toRadians(90));
                }
            }
        }
    }

    public void buildPaths() {
        if (mode == 0) {
            toPassive = follower.pathBuilder()
                    .addPath(new BezierLine(startPose, passivePose))
                    .setLinearHeadingInterpolation(startPose.getHeading(), passivePose.getHeading())
                    .build();
        }
    }

    @Override
    public void init() {
        pathTimer   = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        follower = Constants.createFollower(hardwareMap);

        Drawing.init();

        telemetry.addLine("Initialized. Select team in init_loop.");
        telemetry.update();
    }

    @Override
    public void init_loop() {
        telemetry.addLine("====SELECT STARTING POSITION====");
        telemetry.addLine("Left Action Button: Front of the Blue Goal");
        telemetry.addLine("Top Action Button: Front of the Red Goal");
        telemetry.addLine("Bottom Action Button: Left of the Small Launch Area");
        telemetry.addLine("Right Action Button: Right of the Small Launch Area");
        telemetry.addLine();
        telemetry.addLine("Up D-Pad: Switch Modes");
        telemetry.addLine();

        if (teamSelected) {
            switch (startPosition) {
                case 0:
                    telemetry.addLine("STATUS: Starting at the front of the BLUE goal");
                    break;
                case 1:
                    telemetry.addLine("STATUS: Starting at the front of the RED goal");
                    break;
                case 2:
                    telemetry.addLine("STATUS: Starting at the BLUE small launch area");
                    break;
                default:
                    telemetry.addLine("STATUS: Starting at the RED small launch area");
                    break;
            }
        } else {
            telemetry.addLine("STATUS: Waiting..");
        }

        telemetry.addLine("MODE: " + (mode == 0 ? "Idle" : "Active"));

        if (gamepad1.x) {
            selectedTeam = SharedPoseStorage.Team.BLUE;
            startPosition = 0;
            teamSelected = true;
        } else if (gamepad1.y) {
            selectedTeam = SharedPoseStorage.Team.RED;
            startPosition = 1;
            teamSelected = true;
        } else if (gamepad1.a) {
            selectedTeam = SharedPoseStorage.Team.BLUE;
            startPosition = 2;
            teamSelected = true;
        } else if (gamepad1.b) {
            selectedTeam = SharedPoseStorage.Team.RED;
            startPosition = 3;
            teamSelected = true;
        }

        if (gamepad1.dpadUpWasPressed()) {
            if (mode == 1) {
                mode = 0;
            } else {
                mode = 1;
            }
        }

        telemetry.update();
    }

    @Override
    public void start() {
        if (!teamSelected) {
            throw new IllegalStateException("START POSITION NOT SELECTED! Use the buttons on your controller during initialization to select a position before pressing play!");
        }

        setPosesForTeam();
        follower.setStartingPose(startPose);
        buildPaths();

        SharedPoseStorage.currentTeam  = selectedTeam;
        SharedPoseStorage.teamAvailable = true;

        opmodeTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();

        SharedPoseStorage.currentPose  = follower.getPose();
        SharedPoseStorage.poseAvailable = true;

        telemetry.addData("Path State", pathState);
        telemetry.addData("X",       follower.getPose().getX());
        telemetry.addData("Y",       follower.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.update();

        Drawing.drawDebug(follower);
    }

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                if (mode == 1) {
                    setPathState(1);
                } else {
                    setPathState(10);
                }
                break;

            case 1:
                break;

            case 10:
                follower.followPath(toPassive, true);
                setPathState(11);
                break;

            case 11:
                checkIfBusy(12, 0);
                break;

            case 12:
                break;
        }
    }

    public void checkIfBusy(int nextState, double delaySeconds) {
        if (!follower.isBusy() && pathTimer.getElapsedTimeSeconds() > delaySeconds) {
            setPathState(nextState);
        }
    }

    public void setPathState(int state) {
        pathState = state;
        pathTimer.resetTimer();
    }
}