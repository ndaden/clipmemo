import { ReelNote, AppSettings } from '../types/note';
import { OfflineHeuristicExtractor } from './offlineHeuristicExtractor';

const STORAGE_KEY_NOTES = 'reelnotes_saved_notes_v1';
const STORAGE_KEY_SETTINGS = 'reelnotes_settings_v1';

export const INITIAL_SETTINGS: AppSettings = {
  language: 'fr',
  geminiApiKey: '',
  hasSeenOnboarding: false,
  viewMode: 'responsive',
};

const SEED_NOTES: ReelNote[] = [
  {
    id: 'seed-recipe-carbonara',
    reelUrl: 'https://www.instagram.com/reel/C3zYx7oI8Pq/',
    shortcode: 'C3zYx7oI8Pq',
    author: '@chef_matteo',
    title: 'Vraie Carbonara Italienne Crémeuse',
    category: 'RECIPE',
    summary: 'La véritable recette romaine sans crème : guanciale croustillant, pecorino crémeux et eau de cuisson.',
    rawCaption: `La VRAIE recette des Pâtes Carbonara traditionnelles 🇮🇹✨
Arrêtez avec la crème fraîche et les lardons industriels ! Voici la méthode des trattorias romaines.

Ingrédients (pour 4 personnes) :
- 400g de Spaghetti di Gragnano
- 200g de Guanciale poivré
- 4 jaunes d'œufs extra frais
- 80g de Pecorino Romano râpé très fin
- 1 c. à café de poivre noir en grains concassé

Préparation :
1. Couper le guanciale en lardons d'environ 1 cm d'épaisseur.
2. Faire dorer à sec dans une poêle sans matière grasse à feu doux jusqu'à ce qu'il soit bien doré et croustillant. Réserver le guanciale et garder le gras fondu.
3. Dans un bol en inox, battre les jaunes d'œufs avec la majorité du pecorino et beaucoup de poivre noir pour obtenir une crème dense.
4. Cuire les pâtes dans une eau salée modérément (le pecorino et le guanciale sont déjà salés !).
5. Égoutter les pâtes al dente directement dans la poêle tiède avec le gras du guanciale.
6. Hors du feu, incorporer le mélange d'œufs et une louche d'eau de cuisson bouillante en remuant vivement pour créer la fameuse crème onctueuse.
7. Ajouter le guanciale croustillant, servir immédiatement avec un supplément de pecorino et de poivre noir.

#carbonara #pasta #recette #cuisineitalienne #comfortfood #guanciale`,
    markdownContent: '',
    structuredData: {
      summary: 'La véritable recette romaine sans crème : guanciale croustillant, pecorino crémeux et eau de cuisson.',
      prepTime: '10 min',
      cookTime: '15 min',
      servings: '4 personnes',
      ingredients: [
        { name: 'Spaghetti di Gragnano', amount: '400', unit: 'g', isChecked: true },
        { name: 'Guanciale de porc poivré', amount: '200', unit: 'g', isChecked: true },
        { name: 'Jaunes d’œufs extra frais', amount: '4', unit: '', isChecked: false },
        { name: 'Pecorino Romano râpé', amount: '80', unit: 'g', isChecked: false },
        { name: 'Poivre noir concassé au mortier', amount: '1', unit: 'c. à café', isChecked: false },
      ],
      steps: [
        { stepNumber: 1, instruction: 'Couper le guanciale en lardons de 1 cm d’épaisseur.', isDone: true },
        { stepNumber: 2, instruction: 'Faire dorer à feu doux sans huile jusqu’à croustillant.', isDone: true },
        { stepNumber: 3, instruction: 'Mélanger les jaunes et le pecorino pour former une pâte dense.', isDone: false },
        { stepNumber: 4, instruction: 'Cuire les pâtes al dente dans une eau modérément salée.', isDone: false },
        { stepNumber: 5, instruction: 'Hors du feu, mélanger pâtes, crème d’œufs et eau de cuisson pour émulsionner.', isDone: false },
        { stepNumber: 6, instruction: 'Ajouter les lardons croustillants et servir chaud avec poivre.', isDone: false },
      ],
      keyTakeaways: [
        'Pas de crème ni de lardons industriels',
        'L’onctuosité est obtenue grâce à l’amidon de l’eau de cuisson mélangé aux jaunes hors du feu',
      ],
      tips: [
        'Toujours faire la liaison œuf hors du feu pour éviter de cuire l’œuf en omelette !',
        'Conserver 1 verre d’eau de cuisson avant d’égoutter les pâtes.',
      ],
    },
    thumbnailUrl: 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=600&auto=format&fit=crop&q=80',
    tags: ['carbonara', 'pasta', 'recette', 'cuisineitalienne', 'guanciale'],
    isFavorite: true,
    createdAt: Date.now() - 1000 * 60 * 60 * 48,
    updatedAt: Date.now() - 1000 * 60 * 60 * 48,
  },
  {
    id: 'seed-workout-hiit',
    reelUrl: 'https://www.instagram.com/reel/C5j12z9A0Lp/',
    shortcode: 'C5j12z9A0Lp',
    author: '@coach_alexandre',
    title: 'HIIT Brûle-Graisses 15 Min Sans Matériel',
    category: 'WORKOUT',
    summary: 'Circuit intensif de 5 exercices au poids de corps pour stimuler le métabolisme et le cardio.',
    rawCaption: `Session Express HIIT 15 MIN 🔥 À faire chez soi sans matériel !
Idéal si tu n'as pas le temps d'aller en salle aujourd'hui.

Circuit : 45 secondes d'effort / 15 secondes de repos.
Faire 3 tours au total avec 1 minute de repos entre chaque tour.

Exercices :
1. Jumping Jacks dynamiques pour faire monter le cardio
2. Squats sautés explosifs (atterrissage doux sur la pointe)
3. Mountain Climbers avec genoux rapides vers la poitrine
4. Pompes classiques ou sur les genoux (dos bien droit)
5. Gainage commando (alternance coudes et paumes)

Conseils :
- Rentre le nombril et contracte les abdos sur chaque mouvement.
- Bois des petites gorgées d'eau pendant la minute de pause.

#workout #hiit #fitness #homeworkout #cardio #sport`,
    markdownContent: '',
    structuredData: {
      summary: 'Circuit intensif de 5 exercices au poids de corps pour stimuler le métabolisme et le cardio.',
      prepTime: '2 min',
      cookTime: '15 min',
      servings: '1 session',
      ingredients: [],
      steps: [
        { stepNumber: 1, instruction: 'Jumping Jacks dynamiques (45s effort / 15s repos).', isDone: true },
        { stepNumber: 2, instruction: 'Squats sautés explosifs avec amorti contrôlé (45s / 15s).', isDone: true },
        { stepNumber: 3, instruction: 'Mountain Climbers rythmés genoux vers poitrine (45s / 15s).', isDone: false },
        { stepNumber: 4, instruction: 'Pompes classiques ou adaptées sur genoux (45s / 15s).', isDone: false },
        { stepNumber: 5, instruction: 'Gainage commando coudes/mains en contrôlant le bassin (45s / 15s).', isDone: false },
      ],
      keyTakeaways: [
        'Format : 45 secondes de travail, 15 secondes de pause passive',
        'Total : 3 tours complets, 1 minute de repos entre chaque tour',
        'Objectif : activation cardiovasculaire maximale et renforcement complet',
      ],
      tips: [
        'Maintenez un rythme régulier plutôt que de sprinter dès les 10 premières secondes',
        'Respirez calmement en expirant sur l’effort',
      ],
    },
    thumbnailUrl: 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80',
    tags: ['workout', 'hiit', 'fitness', 'homeworkout', 'cardio'],
    isFavorite: false,
    createdAt: Date.now() - 1000 * 60 * 60 * 24,
    updatedAt: Date.now() - 1000 * 60 * 60 * 24,
  },
  {
    id: 'seed-tips-filmmaking',
    reelUrl: 'https://www.instagram.com/reel/C8qL41_Kz1m/',
    shortcode: 'C8qL41_Kz1m',
    author: '@leo_filmmaker',
    title: '5 Astuces pour Vidéos Cinématiques au Smartphone',
    category: 'TIPS_INFO',
    summary: 'Comment transformer des vidéos iPhone banales en séquences au look cinéma professionnel.',
    rawCaption: `5 Astuces méconnues pour des vidéos de pro avec ton téléphone 📱🎥

1. Verrouille toujours l'exposition (AE/AF Lock) en restant appuyé sur ton sujet. Cela évite que la luminosité change brusquement !
2. Règle ton appareil en 4K à 24 images/seconde (le standard du cinéma).
3. Nettoie la lentille de ta caméra avant chaque prise (la buée et les traces de doigts ruinent le piqué).
4. Déplace-toi en "ninja walk" : genoux pliés, pas feutrés pour remplacer un stabilisateur à 200€.
5. Filme en fin d'après-midi pendant la golden hour pour une lumière douce et dorée.

#iphonehack #filmmaking #cinematic #videotips #astuce #creative`,
    markdownContent: '',
    structuredData: {
      summary: 'Comment transformer des vidéos iPhone banales en séquences au look cinéma professionnel.',
      prepTime: null,
      cookTime: null,
      servings: null,
      ingredients: [],
      steps: [
        { stepNumber: 1, instruction: 'Verrouiller AE/AF Lock pour figer l’exposition', isDone: false },
        { stepNumber: 2, instruction: 'Passer en 4K 24 fps dans Réglages > Appareil photo', isDone: false },
        { stepNumber: 3, instruction: 'Nettoyer la lentille avec microfibre', isDone: false },
        { stepNumber: 4, instruction: 'Adopter la marche ninja pour stabiliser sans gimbal', isDone: false },
      ],
      keyTakeaways: [
        'Le 24fps procure le flou de mouvement (motion blur) propre aux films de cinéma',
        'Le verrouillage d’exposition supprime les sauts d’éclairage disgracieux',
        'La marche ninja fléchie absorbe les secousses verticales',
      ],
      tips: [
        'Activez la grille des tiers pour composer vos plans avec précision',
        'Baissez l’exposition d’un quart de cran pour des noirs plus profonds',
      ],
    },
    thumbnailUrl: 'https://images.unsplash.com/photo-1512499617640-c74ae3a79d37?w=600&auto=format&fit=crop&q=80',
    tags: ['iphonehack', 'filmmaking', 'cinematic', 'astuce'],
    isFavorite: true,
    createdAt: Date.now() - 1000 * 60 * 60 * 12,
    updatedAt: Date.now() - 1000 * 60 * 60 * 12,
  },
  {
    id: 'seed-travel-kyoto',
    reelUrl: 'https://www.instagram.com/reel/C9mK30_P23q/',
    shortcode: 'C9mK30_P23q',
    author: '@eva_explore',
    title: 'Itinéraire 3 Jours Secret à Kyoto',
    category: 'TRAVEL',
    summary: 'Les plus beaux sanctuaires, jardins zen et ruelles préservées loin de la foule à Kyoto.',
    rawCaption: `Guide ultime pour 3 jours parfaits à Kyoto sans la foule ⛩️🍵

Jour 1 :
- Arashiyama dès 7h du matin pour voir la bambouseraie déserte
- Temple Otagi Nenbutsu-ji avec ses 1200 statues de pierre uniques

Jour 2 :
- Randonnée Fushimi Inari en fin d'après-midi jusqu'au sommet
- Balade nocturne dans Gion (quartier des geishas aux lanternes tamisées)

Jour 3 :
- Dégustation de Matcha cérémonial à Uji
- Coucher de soleil sur les marches du temple Kiyomizu-dera

Astuces :
- Prendre une carte Suica / Pasmo sur son téléphone
- Louer des vélos pour parcourir le chemin de la philosophie

#kyoto #japan #voyage #travelguide #explorejapan`,
    markdownContent: '',
    structuredData: {
      summary: 'Les plus beaux sanctuaires, jardins zen et ruelles préservées loin de la foule à Kyoto.',
      prepTime: null,
      cookTime: null,
      servings: null,
      ingredients: [],
      steps: [
        { stepNumber: 1, instruction: 'Jour 1 : Bambouseraie d’Arashiyama à l’aube et temple Otagi Nenbutsu-ji', isDone: false },
        { stepNumber: 2, instruction: 'Jour 2 : Torii de Fushimi Inari au crépuscule puis ruelle de Gion la nuit', isDone: false },
        { stepNumber: 3, instruction: 'Jour 3 : Cérémonie du thé matcha à Uji et temple Kiyomizu-dera', isDone: false },
      ],
      keyTakeaways: [
        'Arriver avant 7h30 du matin sur les sites majeurs garantit une expérience paisible',
        'Le vélo électrique est le meilleur moyen de transport pour découvrir les venelles',
      ],
      tips: [
        'Achetez une carte Suica dématérialisée dans Apple Wallet ou Google Wallet pour les transports',
      ],
    },
    thumbnailUrl: 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=600&auto=format&fit=crop&q=80',
    tags: ['kyoto', 'japan', 'voyage', 'travelguide'],
    isFavorite: false,
    createdAt: Date.now() - 1000 * 60 * 60 * 5,
    updatedAt: Date.now() - 1000 * 60 * 60 * 5,
  },
];

// Generate markdown for seeds
for (const note of SEED_NOTES) {
  note.markdownContent = OfflineHeuristicExtractor.buildMarkdown(
    note.title,
    note.author,
    note.reelUrl,
    note.category,
    note.structuredData,
    note.tags,
    'fr'
  );
}

export const StorageService = {
  getNotes(): ReelNote[] {
    try {
      const data = localStorage.getItem(STORAGE_KEY_NOTES);
      if (!data) {
        this.saveNotes(SEED_NOTES);
        return SEED_NOTES;
      }
      return JSON.parse(data);
    } catch (e) {
      console.error('Failed to read notes from storage:', e);
      return SEED_NOTES;
    }
  },

  saveNotes(notes: ReelNote[]): void {
    try {
      localStorage.setItem(STORAGE_KEY_NOTES, JSON.stringify(notes));
    } catch (e) {
      console.error('Failed to save notes to storage:', e);
    }
  },

  addNote(note: ReelNote): ReelNote[] {
    const existing = this.getNotes();
    const updated = [note, ...existing];
    this.saveNotes(updated);
    return updated;
  },

  updateNote(note: ReelNote): ReelNote[] {
    const existing = this.getNotes();
    const index = existing.findIndex(n => n.id === note.id);
    if (index !== -1) {
      existing[index] = { ...note, updatedAt: Date.now() };
      this.saveNotes(existing);
    }
    return [...existing];
  },

  deleteNote(noteId: string): ReelNote[] {
    const existing = this.getNotes();
    const updated = existing.filter(n => n.id !== noteId);
    this.saveNotes(updated);
    return updated;
  },

  toggleFavorite(noteId: string): ReelNote[] {
    const existing = this.getNotes();
    const note = existing.find(n => n.id === noteId);
    if (note) {
      note.isFavorite = !note.isFavorite;
      note.updatedAt = Date.now();
      this.saveNotes(existing);
    }
    return [...existing];
  },

  getSettings(): AppSettings {
    try {
      const data = localStorage.getItem(STORAGE_KEY_SETTINGS);
      if (!data) return INITIAL_SETTINGS;
      return { ...INITIAL_SETTINGS, ...JSON.parse(data) };
    } catch (e) {
      return INITIAL_SETTINGS;
    }
  },

  saveSettings(settings: AppSettings): void {
    try {
      localStorage.setItem(STORAGE_KEY_SETTINGS, JSON.stringify(settings));
    } catch (e) {
      console.error('Failed to save settings:', e);
    }
  },

  resetToDefaultNotes(): ReelNote[] {
    this.saveNotes(SEED_NOTES);
    return SEED_NOTES;
  },

  exportBackupJson(): string {
    const notes = this.getNotes();
    const settings = this.getSettings();
    return JSON.stringify({ version: '1.2.1', exportedAt: Date.now(), notes, settings }, null, 2);
  },

  importBackupJson(jsonString: string): boolean {
    try {
      const data = JSON.parse(jsonString);
      if (Array.isArray(data.notes)) {
        this.saveNotes(data.notes);
        if (data.settings) {
          this.saveSettings(data.settings);
        }
        return true;
      }
      return false;
    } catch (e) {
      console.error('Failed to parse backup JSON:', e);
      return false;
    }
  },
};
