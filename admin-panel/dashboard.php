<?php
session_start();
if (!isset($_SESSION['admin_user'])) {
    header("Location: login.php");
    exit();
}
include_once __DIR__ . "/dashboard.html";
