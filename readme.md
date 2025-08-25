# 🚀🛰️ Kibo RPC – NeighborhoodCat (RPC6010)

This repository contains our complete solution for the **6th Kibo Robot Programming Challenge (Kibo RPC)**, developed under the team name **NeighborhoodCat**.  
Our code integrates **computer vision, navigation, and AI detection** to autonomously complete the mission onboard JAXA’s **Astrobee robot** on the ISS.

---

## 🏆 Competition Results
- **Team ID**: RPC6010 – *NeighborhoodCat*  
- **Score**: 272.11  
- **Final Standing**: 🥇 8th place in **Thailand** 🇹🇭  

---

📊 Official ranking: [NSTDA Space Education – 6th Kibo RPC Results](https://www.nstda.or.th/spaceeducation/ranking-score-for-the-6th-kibo-rpc/)

---

## 📂 Project Structure
<details open>
  <summary><b>Project Structure (app/src/main/java/jp/jaxa/iss/kibo/rpc/sampleapk/)</b></summary>

<pre>
app/src/main/java/jp/jaxa/iss/kibo/rpc/sampleapk/
├─ MainActivity.java              # Minimal Android activity entrypoint
├─ YourService.java               # Core mission logic (extends KiboRpcService)
│
├─ Oasis/
│  └─ OasisInfo.java              # Mission-specific constants and configs
│
├─ Aruco/
│  ├─ ArucoDetector.java          # Runs OpenCV ArUco detection
│  ├─ ArucoRegion.java            # Region/ROI wrapper for markers
│  └─ PoseEstimator.java          # Estimate pose from ArUco + camera intrinsics
│
├─ Pipeline/
│  ├─ FullPipeline.java           # End-to-end vision → detection pipeline
│  ├─ PipelineDetection.java      # AI detection + NMS postprocessing
│  └─ PipelineImageProcessing.java# Image preprocessing chain
│
├─ Navigate/
│  ├─ Navigator.java              # High-level motion planning
│  └─ WayPoint.java               # Data structure for robot waypoints
│
├─ Camera/
│  ├─ CameraImageData.java        # Image frame representation
│  ├─ CameraImageHandler.java     # Camera capture handler
│  └─ Camera_Type.java            # Enum/class for camera types
│
├─ ImagePreprocessor/
│  ├─ ArucoMasker.java            # Mask background & markers
│  ├─ ArucoSplitter.java          # Split ROIs around markers
│  ├─ Arucoref.java               # Shared constants/utilities
│  ├─ CropFrame.java              # Crop region of interest
│  ├─ ImageRotator.java           # Basic rotation helper
│  ├─ ImageRotationStabilizer.java# Stabilize orientation across frames
│  ├─ ImageUndistorter.java       # Remove lens distortion
│  └─ ItemFrameExtractor.java     # Extract item panels for detection
│
├─ AI/
│  ├─ DetectionResult.java        # Struct for detection outputs
│  ├─ ModelLoader.java            # Loads TensorFlow Lite model
│  ├─ ModelRunner.java            # Runs inference on input frames
│  ├─ NMSProcessor.java           # Non-Maximum Suppression
│  └─ ZoneInfo.java               # Encodes mission "zones"
│
├─ MissionState/
│  └─ MissionState.java           # Mission state machine
│
├─ Robot/
│  └─ RobotState.java             # Robot pose/velocity representation
│
├─ Utils/
│  ├─ ImageUtils.java             # Drawing + image helpers
│  ├─ PointUtils.java             # 2D/3D point math
│  └─ Quaternions.java            # Quaternion math utilities
│
├─ Log/
│  └─ Log.java                    # Simple logger wrapper
└─ .DS_Store                      # macOS metadata (ignore)
</pre>
</details>
