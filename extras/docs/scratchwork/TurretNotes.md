
**OLD CALCULATION METHOD**

``` java
    /**
     * <b> Get Velocity Method </b>
     * <hr>
     * 
     * @return Velocity necessary to shoot the ball into the center of the hub from
     *         the robot's position.
     */
    private double getVelocity() {
        Optional<Double> dx_proxy = hubDistance(); // find distance between robot and hub, in meters

        if (dx_proxy.isEmpty()) {
            return 0.0;
        }
        Optional<Pose3d> poseOpt = vision.getPose3d(getRobotPose());
        if (poseOpt.isEmpty()) {
            return 0.0;
        }

        Pose3d pose = poseOpt.get();
        x = pose.getX();
        y = pose.getY();
        z = pose.getZ();
        heading = pose.getRotation().toRotation2d();

        double dx = dx_proxy.get();
        double dy = ShooterConstants.HUB_RIM_HEIGHT
                - (z + ShooterConstants.Z_OFFSET);
        double g = ShooterConstants.G_ACCEL; // acceleration due to gravity in meters per second squared
        double theta = Math.toRadians(ShooterConstants.LAUNCH_ANGLE);

        return dx / Math.cos(theta)
                * Math.sqrt(g / 2
                        * (-dy + dx * Math.tan(theta)));
    }
```
