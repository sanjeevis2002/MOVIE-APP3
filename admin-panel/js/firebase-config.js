// Official Firebase SDK Web Client Configuration for LIVE TAMIL TV Admin Portal
const firebaseConfig = {
    apiKey: "AIzaSyPlaceholderKeyForLiveTamilTvApp",
    authDomain: "live-tamil-tv-firebase.firebaseapp.com",
    databaseURL: "https://live-tamil-tv-firebase-default-rtdb.firebaseio.com",
    projectId: "live-tamil-tv-firebase",
    storageBucket: "live-tamil-tv-firebase.appspot.com",
    messagingSenderId: "123456789000",
    appId: "1:123456789000:web:abcdef1234567890"
};

// Check if credentials are placeholders or configured
function isFirebaseConfigured() {
    return firebaseConfig.apiKey !== "AIzaSyPlaceholderKeyForLiveTamilTvApp" &&
           !firebaseConfig.apiKey.includes("Placeholder");
}
