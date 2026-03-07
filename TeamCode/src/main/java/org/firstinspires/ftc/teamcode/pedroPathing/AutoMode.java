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

    // -------------------------------------------------------------------------
    // TODO: Define poses here
    // -------------------------------------------------------------------------
    private Pose startPose;
    // private Pose examplePose = new Pose(x, y, Math.toRadians(heading));

    // -------------------------------------------------------------------------
    // TODO: Define path chains here
    // -------------------------------------------------------------------------
    // private PathChain examplePath;

    /** Set starting and intermediate poses based on the selected team/position **/
    private void setPosesForTeam() {
        if (selectedTeam == SharedPoseStorage.Team.RED) {
            startPose = new Pose(0, 0, Math.toRadians(0)); // TODO: Set RED start pose
        } else {
            startPose = new Pose(0, 0, Math.toRadians(0)); // TODO: Set BLUE start pose
        }
    }

    public void buildPaths() {
        // TODO: Build paths here
        // Example:
        // examplePath = follower.pathBuilder()
        //         .addPath(new BezierLine(startPose, examplePose))
        //         .setLinearHeadingInterpolation(startPose.getHeading(), examplePose.getHeading())
        //         .build();
    }

    @Override
    public void init() {
        pathTimer   = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        follower = Constants.createFollower(hardwareMap);

        // TODO: Initialize hardware here
        // e.g. motor = hardwareMap.get(DcMotorEx.class, "motorName");

        Drawing.init();

        telemetry.addLine("Initialized. Select team in init_loop.");
        telemetry.update();
    }

    @Override
    public void init_loop() {
        telemetry.addLine("====SELECT STARTING POSITION====");
        telemetry.addLine("X Button: Blue team");
        telemetry.addLine("B Button: Red team");
        telemetry.addLine();

        if (gamepad1.x) {
            selectedTeam = SharedPoseStorage.Team.BLUE;
            teamSelected = true;
        } else if (gamepad1.b) {
            selectedTeam = SharedPoseStorage.Team.RED;
            teamSelected = true;
        }

        telemetry.addLine("STATUS: " + (teamSelected ? "Team: " + selectedTeam : "Waiting for selection.."));
        telemetry.update();
    }

    @Override
    public void start() {
        if (!teamSelected) {
            throw new IllegalStateException("TEAM NOT SELECTED! Use the controller buttons during initialization to select a team before pressing play!");
        }

        setPosesForTeam();
        follower.setStartingPose(startPose);
        buildPaths();

        // Save team and pose for TeleOp handoff
        SharedPoseStorage.currentTeam  = selectedTeam;
        SharedPoseStorage.teamAvailable = true;

        opmodeTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();

        // Continuously save pose for TeleOp handoff
        SharedPoseStorage.currentPose  = follower.getPose();
        SharedPoseStorage.poseAvailable = true;

        telemetry.addData("Path State", pathState);
        telemetry.addData("X",       follower.getPose().getX());
        telemetry.addData("Y",       follower.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.update();

        Drawing.drawDebug(follower);
    }

    /** State machine — add cases as your routine grows **/
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                // TODO: Start first path and advance state
                // follower.followPath(examplePath, true);
                setPathState(1);
                break;

            case 1:
                // TODO: Wait for path to finish, then advance
                checkIfBusy(2, 0);
                break;

            case 2:
                // TODO: Next action
                break;

            // Add more cases as needed...
        }
    }

    // Helpers

    /** Waits for the follower to finish, then advances to the given state after an optional delay **/
    public void checkIfBusy(int nextState, double delaySeconds) {
        if (!follower.isBusy() && pathTimer.getElapsedTimeSeconds() > delaySeconds) {
            setPathState(nextState);
        }
    }

    /** Sets path state and resets the path timer **/
    public void setPathState(int state) {
        pathState = state;
        pathTimer.resetTimer();
    }
}