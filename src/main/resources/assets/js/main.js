// Smooth scrolling (handled by Tailwind's scroll-smooth class)

// Scroll animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('animated');
        }
    });
}, observerOptions);

document.querySelectorAll('.animate-on-scroll').forEach(el => {
    observer.observe(el);
});

// Typing effect for code
function typeCode() {
    const codeLines = document.querySelectorAll('.code-line');
    let delay = 0;

    codeLines.forEach((line, index) => {
        setTimeout(() => {
            line.style.opacity = '0';
            line.style.transform = 'translateX(-20px)';

            setTimeout(() => {
                line.style.opacity = '1';
                line.style.transform = 'translateX(0)';
            }, 100);
        }, delay);
        delay += 200;
    });
}

// Start typing animation when hero is visible
const heroObserver = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            setTimeout(typeCode, 1000);
            heroObserver.unobserve(entry.target);
        }
    });
});

const hero = document.querySelector('#home');
if (hero) {
    heroObserver.observe(hero);
}

// Dynamic gradient background
function updateGradient() {
    const scrollY = window.scrollY;
    const maxScroll = document.body.scrollHeight - window.innerHeight;
    const scrollPercent = scrollY / maxScroll;

    const bg = document.querySelector('.bg-gradient-custom');
    const hue = scrollPercent * 60;

    bg.style.background = `
        radial-gradient(circle at 20% 50%, hsla(${120 + hue}, 100%, 50%, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, hsla(${320 + hue}, 100%, 50%, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 40% 80%, hsla(${220 + hue}, 100%, 50%, 0.1) 0%, transparent 50%)
    `;
}

// Parallax effect
function handleParallax() {
    const scrollY = window.scrollY;
    const elements = document.querySelectorAll('.project-card, .skill-card');

    elements.forEach((el, index) => {
        const speed = 0.5 + (index * 0.1);
        const rect = el.getBoundingClientRect();

        if (rect.top < window.innerHeight && rect.bottom > 0) {
            const yPos = -(scrollY * speed);
            el.style.transform = `translateY(${yPos * 0.1}px)`;
        }
    });
}

// Mouse movement effect for 3D tilt
function handleMouseMove(e) {
    const cards = document.querySelectorAll('.project-card, .skill-card, .bg-bg-card');

    cards.forEach(card => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        if (x >= 0 && x <= rect.width && y >= 0 && y <= rect.height) {
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;

            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
        } else {
            card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateZ(0px)';
        }
    });
}

// Navigation highlight on scroll
function updateNavigation() {
    const sections = document.querySelectorAll('section[id]');
    const navLinks = document.querySelectorAll('nav a[href^="#"]');

    let current = '';

    sections.forEach(section => {
        const sectionTop = section.getBoundingClientRect().top;
        if (sectionTop <= 100) {
            current = section.getAttribute('id');
        }
    });

    navLinks.forEach(link => {
        link.classList.remove('text-accent');
        link.classList.add('text-text-secondary');
        if (link.getAttribute('href') === `#${current}`) {
            link.classList.remove('text-text-secondary');
            link.classList.add('text-accent');
        }
    });
}

// Event listeners
window.addEventListener('scroll', () => {
    updateGradient();
    handleParallax();
    updateNavigation();
});

document.addEventListener('mousemove', handleMouseMove);

// Initialize
updateGradient();
updateNavigation();

// Loading animation
window.addEventListener('load', () => {
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.5s ease';

    setTimeout(() => {
        document.body.style.opacity = '1';
    }, 100);
});

// Easter egg - Konami code
let konamiCode = [];
const konami = [38, 38, 40, 40, 37, 39, 37, 39, 66, 65]; // ↑↑↓↓←→←→BA

document.addEventListener('keydown', (e) => {
    konamiCode.push(e.keyCode);
    if (konamiCode.length > konami.length) {
        konamiCode.shift();
    }

    if (JSON.stringify(konamiCode) === JSON.stringify(konami)) {
        // Secret rainbow animation
        document.body.style.animation = 'rainbow 2s ease-in-out';
        setTimeout(() => {
            document.body.style.animation = '';
        }, 2000);
    }
});

// Mobile menu toggle (if needed)
function toggleMobileMenu() {
    const menu = document.querySelector('.nav-menu');
    menu.classList.toggle('hidden');
}

// Add mobile menu button functionality if screen is small
if (window.innerWidth <= 768) {
    const nav = document.querySelector('nav .max-w-6xl');
    const mobileButton = document.createElement('button');
    mobileButton.innerHTML = `
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
        </svg>
    `;
    mobileButton.className = 'md:hidden text-text-primary';
    mobileButton.onclick = toggleMobileMenu;
    nav.appendChild(mobileButton);
}

// Intersection Observer for project cards animation
const projectObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.animationDelay = `${entry.target.dataset.delay || 0}ms`;
            entry.target.classList.add('animate-fade-in-up');
        }
    });
}, { threshold: 0.1 });

document.querySelectorAll('.project-card').forEach((card, index) => {
    card.dataset.delay = index * 100;
    projectObserver.observe(card);
});

// Skills cards stagger animation
const skillObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.animationDelay = `${entry.target.dataset.delay || 0}ms`;
            entry.target.classList.add('animate-fade-in-up');
        }
    });
}, { threshold: 0.1 });

document.querySelectorAll('.skill-card').forEach((card, index) => {
    card.dataset.delay = index * 150;
    skillObserver.observe(card);
});

// Add scroll indicator
function addScrollIndicator() {
    const indicator = document.createElement('div');
    indicator.className = 'fixed top-0 left-0 w-full h-1 bg-gradient-to-r from-accent to-accent-secondary z-50 origin-left scale-x-0 transition-transform duration-300';
    document.body.appendChild(indicator);

    window.addEventListener('scroll', () => {
        const scrollPercent = window.scrollY / (document.body.scrollHeight - window.innerHeight);
        indicator.style.transform = `scaleX(${scrollPercent})`;
    });
}

// Initialize scroll indicator
addScrollIndicator();