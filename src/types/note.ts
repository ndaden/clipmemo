export type NoteCategory =
  | 'RECIPE'
  | 'TUTORIAL'
  | 'WORKOUT'
  | 'TIPS_INFO'
  | 'TRAVEL'
  | 'PRODUCT'
  | 'GENERAL';

export interface IngredientItem {
  name: string;
  amount?: string;
  unit?: string;
  isChecked?: boolean;
}

export interface StepItem {
  stepNumber: number;
  instruction: string;
  isDone?: boolean;
}

export interface StructuredNoteData {
  summary: string;
  ingredients: IngredientItem[];
  steps: StepItem[];
  keyTakeaways: string[];
  prepTime?: string | null;
  cookTime?: string | null;
  servings?: string | null;
  tips: string[];
}

export interface ReelNote {
  id: string; // or number/timestamp
  reelUrl: string;
  shortcode: string;
  author?: string | null;
  title: string;
  category: NoteCategory;
  summary: string;
  rawCaption: string;
  markdownContent: string;
  structuredData: StructuredNoteData;
  thumbnailUrl?: string | null;
  tags: string[];
  isFavorite: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface CategoryInfo {
  id: NoteCategory;
  labelFr: string;
  labelEn: string;
  iconEmoji: string;
  color: string;
  badgeBg: string;
  borderAccent: string;
}

export const CATEGORIES_CONFIG: Record<NoteCategory, CategoryInfo> = {
  RECIPE: {
    id: 'RECIPE',
    labelFr: 'Recette',
    labelEn: 'Recipe',
    iconEmoji: '🍳',
    color: '#10B981',
    badgeBg: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
    borderAccent: 'border-l-emerald-500',
  },
  TUTORIAL: {
    id: 'TUTORIAL',
    labelFr: 'Tutoriel',
    labelEn: 'Tutorial',
    iconEmoji: '🛠️',
    color: '#06B6D4',
    badgeBg: 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30',
    borderAccent: 'border-l-cyan-500',
  },
  WORKOUT: {
    id: 'WORKOUT',
    labelFr: 'Sport',
    labelEn: 'Workout',
    iconEmoji: '💪',
    color: '#F97316',
    badgeBg: 'bg-orange-500/15 text-orange-400 border-orange-500/30',
    borderAccent: 'border-l-orange-500',
  },
  TIPS_INFO: {
    id: 'TIPS_INFO',
    labelFr: 'Astuce',
    labelEn: 'Tips',
    iconEmoji: '💡',
    color: '#F59E0B',
    badgeBg: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
    borderAccent: 'border-l-amber-500',
  },
  TRAVEL: {
    id: 'TRAVEL',
    labelFr: 'Voyage',
    labelEn: 'Travel',
    iconEmoji: '✈️',
    color: '#3B82F6',
    badgeBg: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
    borderAccent: 'border-l-blue-500',
  },
  PRODUCT: {
    id: 'PRODUCT',
    labelFr: 'Produit',
    labelEn: 'Product',
    iconEmoji: '🛍️',
    color: '#EC4899',
    badgeBg: 'bg-pink-500/15 text-pink-400 border-pink-500/30',
    borderAccent: 'border-l-pink-500',
  },
  GENERAL: {
    id: 'GENERAL',
    labelFr: 'Autre',
    labelEn: 'General',
    iconEmoji: '📝',
    color: '#8B5CF6',
    badgeBg: 'bg-purple-500/15 text-purple-400 border-purple-500/30',
    borderAccent: 'border-l-purple-500',
  },
};

export interface AppSettings {
  language: 'fr' | 'en';
  geminiApiKey: string;
  hasSeenOnboarding: boolean;
  viewMode: 'responsive' | 'mobile-mockup';
}
