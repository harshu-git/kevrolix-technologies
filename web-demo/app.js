// PrivacyView Prototype State Manager & Real Optics Simulation
const state = {
  isPrivacyEnabled: false,
  privacyMode: 'BLUR', // 'BLUR' (FLAG_BLUR_BEHIND / Spatial Mesh) | 'DARK' (Luminance Clamping)
  strength: 0.65,      // 0.15 - 0.95
  viewingAngle: 0,     // -75 to +75 deg
  currentScreen: 'privacy-app'
};

// DOM Elements
const phoneContainer = document.getElementById('phone-container');
const angleSlider = document.getElementById('angle-slider');
const angleLabel = document.getElementById('angle-label');
const angleOcclusionTag = document.getElementById('angle-occlusion-tag');
const anglePhysicsCaption = document.getElementById('angle-physics-caption');

// Screen elements
const screenViews = {
  'privacy-app': document.getElementById('view-privacy-app'),
  'home-widget': document.getElementById('view-home-widget'),
  'quick-settings': document.getElementById('view-quick-settings'),
  'banking-app': document.getElementById('view-banking-app')
};

const appScreenSelector = document.getElementById('app-screen-selector');

// App Controls
const heroToggleBtn = document.getElementById('hero-toggle-btn');
const heroBtnText = document.getElementById('hero-btn-text');
const heroStatusDot = document.getElementById('hero-status-dot');
const heroStatusText = document.getElementById('hero-status-text');
const heroStatusDesc = document.getElementById('hero-status-desc');

const modeBlurBtn = document.getElementById('mode-blur-btn');
const modeDarkBtn = document.getElementById('mode-dark-btn');
const modePillIndicator = document.getElementById('mode-pill-indicator');
const modeExplanation = document.getElementById('mode-explanation');

const strengthSlider = document.getElementById('strength-slider');
const strengthDisplay = document.getElementById('strength-display');

// System Integrations
const statusPrivacyDot = document.getElementById('status-privacy-dot');
const privacyOverlaySystem = document.getElementById('privacy-overlay-system');

// Widget Elements
const widgetToggleAction = document.getElementById('widget-toggle-action');
const widgetDot = document.getElementById('widget-dot');
const widgetLabel = document.getElementById('widget-label');

// Quick Settings Elements
const qsPrivacyTile = document.getElementById('qs-privacy-tile');
const qsTileSub = document.getElementById('qs-tile-sub');
const qsOngoingNotif = document.getElementById('qs-ongoing-notif');

// List Navigation
const rowOpenWidget = document.getElementById('row-open-widget');
const rowOpenQs = document.getElementById('row-open-qs');
const rowOpenCalibration = document.getElementById('row-open-calibration');

// Initialize
function init() {
  setupEventListeners();
  updateUI();
  updatePhoneAngle();
}

function setupEventListeners() {
  // Viewing Angle
  angleSlider.addEventListener('input', (e) => {
    state.viewingAngle = parseInt(e.target.value, 10);
    updatePhoneAngle();
  });

  // App Screen Switcher
  appScreenSelector.querySelectorAll('.pill-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const target = btn.dataset.screen;
      setScreen(target);
    });
  });

  // Hero Toggle
  heroToggleBtn.addEventListener('click', togglePrivacy);

  // Widget Toggle
  widgetToggleAction.addEventListener('click', (e) => {
    e.stopPropagation();
    togglePrivacy();
  });

  // Quick Settings Tile
  qsPrivacyTile.addEventListener('click', togglePrivacy);

  // App Grid Icons
  document.querySelectorAll('.app-icon-item').forEach((item) => {
    item.addEventListener('click', () => {
      const app = item.dataset.app;
      if (app === 'banking') setScreen('banking-app');
      else if (app === 'privacy') setScreen('privacy-app');
    });
  });

  // Mode Selection
  modeBlurBtn.addEventListener('click', () => setMode('BLUR'));
  modeDarkBtn.addEventListener('click', () => setMode('DARK'));

  // Strength Slider
  strengthSlider.addEventListener('input', (e) => {
    state.strength = parseInt(e.target.value, 10) / 100;
    strengthDisplay.textContent = `${parseInt(e.target.value, 10)}%`;
    updateOverlayPhysics();
  });

  // Navigation rows in main app
  rowOpenWidget.addEventListener('click', () => setScreen('home-widget'));
  rowOpenQs.addEventListener('click', () => setScreen('quick-settings'));
  rowOpenCalibration.addEventListener('click', () => setScreen('banking-app'));
}

function togglePrivacy() {
  state.isPrivacyEnabled = !state.isPrivacyEnabled;
  updateUI();
}

function setMode(mode) {
  state.privacyMode = mode;
  updateUI();
}

function setScreen(screenId) {
  state.currentScreen = screenId;
  Object.keys(screenViews).forEach((id) => {
    screenViews[id].classList.toggle('active', id === screenId);
  });
  appScreenSelector.querySelectorAll('.pill-btn').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.screen === screenId);
  });
}

function updateUI() {
  const isEnabled = state.isPrivacyEnabled;

  // Status Bar Dot
  statusPrivacyDot.classList.toggle('active', isEnabled);

  // Hero Card
  heroStatusDot.classList.toggle('active', isEnabled);
  heroToggleBtn.classList.toggle('active', isEnabled);
  heroStatusText.textContent = isEnabled ? 'Privacy is ON' : 'Privacy is OFF';
  heroBtnText.textContent = isEnabled ? 'Turn Off Privacy' : 'Turn On Privacy';
  heroStatusDesc.textContent = isEnabled
    ? 'Screen protection is active across your device.'
    : 'Tap to obscure screen viewing from side angles.';

  // Mode Buttons & Explanations
  const isBlur = state.privacyMode === 'BLUR';
  modeBlurBtn.classList.toggle('active', isBlur);
  modeDarkBtn.classList.toggle('active', !isBlur);
  modePillIndicator.style.transform = isBlur ? 'translateX(0)' : 'translateX(100%)';
  modeExplanation.textContent = isBlur
    ? 'FLAG_BLUR_BEHIND on Android 12+ or spatial micro-lattice camouflage.'
    : 'Polarized luminance clamping to suppress off-axis panel viewing cones.';

  // Widget
  widgetDot.classList.toggle('active', isEnabled);
  widgetLabel.textContent = isEnabled ? 'Privacy ON' : 'Privacy OFF';

  // Quick Settings Tile
  qsPrivacyTile.classList.toggle('active', isEnabled);
  qsTileSub.textContent = isEnabled ? `${state.privacyMode === 'BLUR' ? 'Blur' : 'Dark'} • ${Math.round(state.strength * 100)}%` : 'Off';
  qsOngoingNotif.classList.toggle('active', isEnabled);

  // Overlay Styling
  privacyOverlaySystem.classList.toggle('active', isEnabled);
  privacyOverlaySystem.classList.toggle('mode-blur', state.privacyMode === 'BLUR');
  privacyOverlaySystem.classList.toggle('mode-dark', state.privacyMode === 'DARK');

  updateOverlayPhysics();
}

function updatePhoneAngle() {
  const angle = state.viewingAngle;
  phoneContainer.style.transform = `rotateY(${angle}deg) rotateX(${Math.abs(angle) * 0.1}deg)`;

  const absAngle = Math.abs(angle);
  const side = angle < 0 ? 'Left' : 'Right';
  angleLabel.textContent = angle === 0 ? '0° (Direct Front)' : `${absAngle}° (${side} Angle)`;

  // Realistic physical occlusion explanation
  let occlusionText = '100% Readable';
  let badgeClass = 'badge-front';
  let captionText = '';

  if (state.isPrivacyEnabled) {
    if (absAngle < 20) {
      occlusionText = 'High Readability (Front)';
      badgeClass = 'badge-front';
      captionText = 'Direct line-of-sight: User proximity & perpendicular alignment preserve readability even through luminance attenuation.';
    } else if (absAngle < 45) {
      occlusionText = 'Degraded Contrast (45°)';
      badgeClass = 'badge-side';
      captionText = 'Intermediate angle: OLED/LCD off-axis luminance falloff begins to compound with software dimming, reducing word legibility.';
    } else {
      occlusionText = 'Obscured by Glare & Falloff (75°)';
      badgeClass = 'badge-side';
      captionText = 'Extreme angle: 70% native panel luminance loss + ambient surface reflection push effective contrast below the human visual threshold.';
    }
  } else {
    occlusionText = absAngle > 45 ? 'Readable (No Privacy)' : '100% Readable';
    badgeClass = 'badge-front';
    captionText = 'Privacy OFF: High screen luminance makes text readable across wide angles unless software dimming is engaged.';
  }

  angleOcclusionTag.textContent = occlusionText;
  angleOcclusionTag.className = `badge ${badgeClass}`;
  anglePhysicsCaption.textContent = captionText;

  updateOverlayPhysics();
}

/**
 * Accurately simulates the genuine physical optics of mobile displays:
 * Software cannot bend photons. Instead:
 * 1. Base display emission: Lambertian pattern.
 * 2. Off-axis panel falloff: Display panels lose 50-70% luminance at 60°+.
 * 3. Software layer: Dark tint or high-frequency mesh reduces base luminance.
 * 4. Ambient reflection: Room light reflecting off the outer cover glass washes out low-luminance text at oblique angles.
 */
function updateOverlayPhysics() {
  if (!state.isPrivacyEnabled) {
    privacyOverlaySystem.style.opacity = '0';
    return;
  }

  const absAngle = Math.abs(state.viewingAngle);
  const angleFactor = Math.min(1, absAngle / 70);
  const baseStrength = state.strength;

  if (state.privacyMode === 'BLUR') {
    // If Android 12+ FLAG_BLUR_BEHIND or spatial camouflage is active
    const blurPx = Math.max(0, (0.5 + angleFactor * 1.5) * 8 * baseStrength);
    document.documentElement.style.setProperty('--overlay-blur', `${blurPx.toFixed(1)}px`);
    // Camouflage mesh combined with dimming
    const meshOpacity = Math.min(0.95, 0.35 + (angleFactor * 0.55) * baseStrength);
    privacyOverlaySystem.style.opacity = `${meshOpacity.toFixed(2)}`;
  } else {
    // Dark mode: luminance clamping
    // Simulates the compound effect of software dimming + native panel off-axis falloff
    const darkAlpha = Math.min(0.96, (0.35 + (angleFactor * 0.60)) * baseStrength);
    document.documentElement.style.setProperty('--overlay-dark-alpha', darkAlpha.toFixed(2));
    privacyOverlaySystem.style.opacity = '1';
  }
}

// Start
window.addEventListener('DOMContentLoaded', init);
