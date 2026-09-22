/**
 * KEVROLIX TECHNOLOGIES — OFFICIAL CLIENT LOGIC
 * Brand: Kevrolix Technologies | Tagline: "Ideas to Reality"
 * Features: Restrained ambient particle canvas, scroll-triggered reveals, mobile menu, phone mockup telemetry, contact form handling.
 */

document.addEventListener('DOMContentLoaded', () => {
  // =========================================================================
  // 1. RESTRAINED AMBIENT BACKGROUND CANVAS
  // =========================================================================
  const canvas = document.getElementById('ambient-canvas');
  if (canvas) {
    const ctx = canvas.getContext('2d');
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    window.addEventListener('resize', () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    });

    // Subtle, slow-drifting atmospheric nodes
    const NODE_COUNT = Math.min(Math.floor(window.innerWidth / 35), 45);
    const nodes = [];

    for (let i = 0; i < NODE_COUNT; i++) {
      nodes.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.25,
        vy: (Math.random() - 0.5) * 0.25,
        radius: Math.random() * 1.5 + 0.5,
        alpha: Math.random() * 0.35 + 0.1,
      });
    }

    let animationFrameId;
    let isVisible = true;

    document.addEventListener('visibilitychange', () => {
      isVisible = !document.hidden;
      if (isVisible) loop();
    });

    function loop() {
      if (!isVisible) return;
      ctx.clearRect(0, 0, width, height);

      // Draw subtle nodes
      for (let i = 0; i < nodes.length; i++) {
        const n = nodes[i];
        n.x += n.vx;
        n.y += n.vy;

        if (n.x < 0) n.x = width;
        if (n.x > width) n.x = 0;
        if (n.y < 0) n.y = height;
        if (n.y > height) n.y = 0;

        ctx.beginPath();
        ctx.arc(n.x, n.y, n.radius, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(0, 210, 255, ${n.alpha})`;
        ctx.fill();

        // Connect nearby nodes with faint lines
        for (let j = i + 1; j < nodes.length; j++) {
          const n2 = nodes[j];
          const dist = Math.hypot(n.x - n2.x, n.y - n2.y);
          if (dist < 130) {
            ctx.beginPath();
            ctx.moveTo(n.x, n.y);
            ctx.lineTo(n2.x, n2.y);
            ctx.strokeStyle = `rgba(0, 210, 255, ${0.08 * (1 - dist / 130)})`;
            ctx.lineWidth = 0.6;
            ctx.stroke();
          }
        }
      }

      animationFrameId = requestAnimationFrame(loop);
    }

    loop();
  }

  // =========================================================================
  // 2. HEADER SCROLL EFFECT & ACTIVE NAVIGATION
  // =========================================================================
  const header = document.getElementById('header');
  const sections = document.querySelectorAll('section[id]');
  const navLinks = document.querySelectorAll('.nav-link');

  window.addEventListener('scroll', () => {
    if (window.scrollY > 30) {
      header?.classList.add('scrolled');
    } else {
      header?.classList.remove('scrolled');
    }

    // Highlight current section in navbar
    let currentId = '';
    const scrollPos = window.scrollY + 140;

    sections.forEach((sec) => {
      const top = sec.offsetTop;
      const height = sec.offsetHeight;
      if (scrollPos >= top && scrollPos < top + height) {
        currentId = sec.getAttribute('id');
      }
    });

    navLinks.forEach((link) => {
      link.classList.remove('active');
      if (link.getAttribute('href') === `#${currentId}`) {
        link.classList.add('active');
      }
    });
  });

  // =========================================================================
  // 3. MOBILE MENU TOGGLE
  // =========================================================================
  const mobileToggle = document.getElementById('mobile-toggle');
  if (mobileToggle) {
    mobileToggle.addEventListener('click', () => {
      document.body.classList.toggle('mobile-menu-active');
    });
  }

  // Close menu upon navigation click
  navLinks.forEach((link) => {
    link.addEventListener('click', () => {
      document.body.classList.remove('mobile-menu-active');
    });
  });

  // =========================================================================
  // 4. SCROLL REVEAL OBSERVER
  // =========================================================================
  const revealElements = document.querySelectorAll('.reveal-on-scroll');
  if ('IntersectionObserver' in window) {
    const revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            revealObserver.unobserve(entry.target);
          }
        });
      },
      {
        threshold: 0.12,
        rootMargin: '0px 0px -40px 0px',
      }
    );

    revealElements.forEach((el) => revealObserver.observe(el));
  } else {
    revealElements.forEach((el) => el.classList.add('revealed'));
  }

  // =========================================================================
  // 5. PHONE MOCKUP TIME TELEMETRY
  // =========================================================================
  const phoneClock = document.getElementById('phone-clock');
  function updatePhoneClock() {
    if (phoneClock) {
      const now = new Date();
      phoneClock.textContent = now.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
    }
  }
  setInterval(updatePhoneClock, 1000);
  updatePhoneClock();

  // =========================================================================
  // 6. CONTACT FORM VALIDATION & SERVICE HOOK
  // =========================================================================
  const contactForm = document.getElementById('contact-form');
  const formSuccess = document.getElementById('form-success');
  const formError = document.getElementById('form-error');
  const formErrorText = document.getElementById('form-error-text');

  if (contactForm) {
    contactForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      if (formSuccess) formSuccess.style.display = 'none';
      if (formError) formError.style.display = 'none';

      const name = document.getElementById('contact-name')?.value.trim();
      const email = document.getElementById('contact-email')?.value.trim();
      const subject = document.getElementById('contact-subject')?.value.trim();
      const message = document.getElementById('contact-message')?.value.trim();

      // Basic client-side validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!name || !email || !subject || !message) {
        showError('Please complete all fields before sending.');
        return;
      }

      if (!emailRegex.test(email)) {
        showError('Please provide a valid email address.');
        return;
      }

      /**
       * BACKEND EMAIL SERVICE INTEGRATION:
       * When ready to connect a live email service (e.g. Formspree, EmailJS, Resend, or custom API),
       * replace the mock handler below with your fetch() endpoint.
       *
       * Example:
       * const response = await fetch('https://formspree.io/f/YOUR_FORM_ID', {
       *   method: 'POST',
       *   headers: { 'Content-Type': 'application/json' },
       *   body: JSON.stringify({ name, email, subject, message })
       * });
       */

      // Clean local client confirmation
      if (formSuccess) {
        formSuccess.style.display = 'flex';
        formSuccess.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }

      // Also provide a direct pre-composed mailto fallback link in console
      const mailtoUrl = `mailto:kevrolix@gmail.com?subject=${encodeURIComponent(
        `[Inquiry] ${subject}`
      )}&body=${encodeURIComponent(
        `Name: ${name}\nEmail: ${email}\n\nMessage:\n${message}`
      )}`;
      console.log('Contact message ready for dispatch. Mailto fallback:', mailtoUrl);

      contactForm.reset();
    });
  }

  function showError(msg) {
    if (formError && formErrorText) {
      formErrorText.textContent = msg;
      formError.style.display = 'flex';
      formError.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }
});
