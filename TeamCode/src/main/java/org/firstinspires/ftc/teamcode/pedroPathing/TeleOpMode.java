package org.firstinspires.ftc.teamcode.pedroPathing;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@Configurable
@TeleOp
public class TeleOpMode extends OpMode {
    private Follower follower;
    private TelemetryManager telemetryM;

    /** Constants **/
    double microSpeed = 0.10;
    double regularSpeed = 0.80;
    double turnSpeed = 0.50;

    // Quick Rotation
    private boolean isRotatingToTarget = false;
    private double targetHeading = 0;
    private boolean rightStickPressed = false;
    double quickRotationAngle = 180.0; // degrees

    // Positioning
    private boolean teamSelected = false;

    @Override
    public void init() {
        follower  = Constants.createFollower(hardwareMap);
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        DcMotor leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        follower.setStartingPose(new Pose());
        follower.update();

        Drawing.init();
    }

    @Override
    public void start() {
        // Load pose from Autonomous if available
        if (SharedPoseStorage.poseAvailable) {
            follower.setStartingPose(SharedPoseStorage.currentPose);
            telemetry.addLine("Loaded pose from Autonomous!");
            telemetry.addData("X",       SharedPoseStorage.currentPose.getX());
            telemetry.addData("Y",       SharedPoseStorage.currentPose.getY());
            telemetry.addData("Heading", Math.toDegrees(SharedPoseStorage.currentPose.getHeading()));
        }

        // Load team from Autonomous if available
        if (SharedPoseStorage.teamAvailable) {
            teamSelected = true;
        }

        telemetry.update();
        follower.startTeleopDrive(true);
    }

    @Override
    public void loop() {
        follower.update();
        telemetryM.update();

        // --- Movement ---
        double line   = -gamepad1.left_stick_y  * regularSpeed;
        double strafe = -gamepad1.left_stick_x  * regularSpeed;
        double turn   = -gamepad1.right_stick_x * turnSpeed;

        // D-Pad micro movement
        if (gamepad1.dpad_up) { line = microSpeed;  strafe = 0; }
        else if (gamepad1.dpad_down)  { line = -microSpeed; strafe = 0; }
        else if (gamepad1.dpad_right) { line = 0; strafe = -microSpeed; }
        else if (gamepad1.dpad_left)  { line = 0; strafe =  microSpeed; }

        // Bumper micro rotation
        if (gamepad1.right_bumper) turn = -microSpeed;
        else if (gamepad1.left_bumper)  turn =  microSpeed;

        // Quick 180° rotation (right stick click)
        if (gamepad1.right_stick_button && !rightStickPressed && !isRotatingToTarget) {
            rightStickPressed  = true;
            double currentDeg  = Math.toDegrees(follower.getPose().getHeading());
            targetHeading      = Math.toRadians(currentDeg - quickRotationAngle);
            isRotatingToTarget = true;
        } else if (!gamepad1.right_stick_button) {
            rightStickPressed = false;
        }

        if (isRotatingToTarget) {
            double headingError = targetHeading - follower.getPose().getHeading();
            while (headingError >  Math.PI) headingError -= 2 * Math.PI;
            while (headingError < -Math.PI) headingError += 2 * Math.PI;

            if (Math.abs(Math.toDegrees(headingError)) < 1.0) {
                turn = 0;
                isRotatingToTarget = false;
            } else {
                turn = headingError * 0.5;
            }
        }

        follower.setTeleOpDrive(line, strafe, turn, true);

        telemetryUpdate();
    }

    @Override
    public void stop() {
        SharedPoseStorage.poseAvailable = false;
        SharedPoseStorage.teamAvailable = false;
        super.stop();
    }

    private void telemetryUpdate() {
        telemetry.addLine("====ROBOT INFO====");
        telemetry.addData("Movement Speed", regularSpeed);
        telemetry.addData("Turning Speed",  turnSpeed);

        if (teamSelected) {
            telemetry.addLine("\n====POSITIONING====");
            telemetry.addData("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
            telemetry.addData("X", follower.getPose().getX());
            telemetry.addData("Y", follower.getPose().getY());
            Drawing.drawDebug(follower);
        }

        telemetry.addLine("\n====CONTROLS====");
        telemetry.addLine("Left Joystick: Movement");
        telemetry.addLine("Right Joystick: Rotation");
        telemetry.addLine("Right Joystick Button: Rotate 180° clockwise");
        telemetry.addLine("D-Pad: Micro movement");
        telemetry.addLine("Left + Right Bumper: Micro rotation");

        telemetry.update();
    }
}