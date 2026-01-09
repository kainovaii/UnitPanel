tailwind.config = {
    theme: {
        extend: {
            fontFamily: {
                'inter': ['Inter', 'sans-serif'],
            },
            colors: {
                // --- Base Couleurs Sombres (Inspiré du fond Violet/Noir du logo) ---
                'bg-primary': '#190F30',    // Violet foncé, presque noir
                'bg-secondary': '#281C4F',  // Variante un peu plus claire
                'bg-card': '#3A2C68',       // Arrière-plan des cartes
                'text-primary': '#F0F0FF',
                'text-secondary': '#C4BCE8', // Gris-lavande doux pour la lecture
                'border-color': '#5C4F8D',  // Bordures violacées

                // --- Couleurs d'Accent (Extraction Directe du Logo) ---
                'accent-main': '#FF4D94',     // Rose/Magenta du logo (pour le dégradé 'from')
                'accent-secondary': '#FF00FF', // Magenta Pur (Accent non principal)
                'accent-tertiary': '#FF8833',  // Orange Vif du logo (pour le dégradé 'to' et liens)
                'accent-neon': '#30FF66',    // Vert Émeraude Néon (pour le code)
            },
            animation: {
                'bg-move': 'bgMove 20s ease-in-out infinite',
                'fade-in-up': 'fadeInUp 0.8s ease forwards',
                'rainbow': 'rainbow 2s ease-in-out',
            },
            keyframes: {
                bgMove: {
                    '0%, 100%': { transform: 'translateY(0px) rotate(0deg)' },
                    '50%': { transform: 'translateY(-20px) rotate(1deg)' }
                },
                fadeInUp: {
                    'from': { opacity: '0', transform: 'translateY(50px)' },
                    'to': { opacity: '1', transform: 'translateY(0)' }
                },
                rainbow: {
                    '0%': { filter: 'hue-rotate(0deg)' },
                    '100%': { filter: 'hue-rotate(360deg)' }
                }
            }
        }
    }
}