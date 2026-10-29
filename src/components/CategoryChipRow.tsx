import React from 'react';
import { NoteCategory, CATEGORIES_CONFIG } from '../types/note';

interface CategoryChipRowProps {
  selectedCategory: NoteCategory | null;
  onSelectCategory: (category: NoteCategory | null) => void;
  categoryCounts: Record<string, number>;
  totalCount: number;
  language: 'fr' | 'en';
}

export const CategoryChipRow: React.FC<CategoryChipRowProps> = ({
  selectedCategory,
  onSelectCategory,
  categoryCounts,
  totalCount,
  language,
}) => {
  const isFr = language === 'fr';

  const categories: (NoteCategory | null)[] = [
    null,
    'RECIPE',
    'TUTORIAL',
    'WORKOUT',
    'TIPS_INFO',
    'TRAVEL',
    'PRODUCT',
    'GENERAL',
  ];

  return (
    <div className="flex items-center gap-2 overflow-x-auto py-2 px-1 no-scrollbar select-none">
      {categories.map(cat => {
        const isSelected = selectedCategory === cat;
        const count = cat === null ? totalCount : categoryCounts[cat] || 0;
        const config = cat ? CATEGORIES_CONFIG[cat] : null;

        const label = cat === null ? (isFr ? 'Tous' : 'All') : isFr ? config!.labelFr : config!.labelEn;
        const emoji = cat === null ? '✨' : config!.iconEmoji;

        return (
          <button
            key={cat || 'all'}
            onClick={() => onSelectCategory(cat)}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all duration-200 shrink-0 cursor-pointer ${
              isSelected
                ? 'bg-gradient-to-r from-violet-600 to-purple-600 text-white shadow-md shadow-violet-500/25 border border-violet-400/40 scale-[1.02]'
                : 'bg-[#14161D]/90 text-slate-400 hover:text-white hover:bg-[#1C1F2B] border border-[#232733]'
            }`}
          >
            <span>{emoji}</span>
            <span>{label}</span>
            <span
              className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                isSelected
                  ? 'bg-white/20 text-white'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              {count}
            </span>
          </button>
        );
      })}
    </div>
  );
};
