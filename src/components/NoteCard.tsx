import React from 'react';
import { Star, ExternalLink, Trash2, Clock, Users, Flame } from 'lucide-react';
import { ReelNote, CATEGORIES_CONFIG } from '../types/note';

interface NoteCardProps {
  note: ReelNote;
  onSelect: (note: ReelNote) => void;
  onToggleFavorite: (e: React.MouseEvent, noteId: string) => void;
  onDelete: (e: React.MouseEvent, noteId: string) => void;
  language: 'fr' | 'en';
}

export const NoteCard: React.FC<NoteCardProps> = ({
  note,
  onSelect,
  onToggleFavorite,
  onDelete,
  language,
}) => {
  const isFr = language === 'fr';
  const catConfig = CATEGORIES_CONFIG[note.category] || CATEGORIES_CONFIG.GENERAL;

  const totalIngredients = note.structuredData?.ingredients?.length || 0;
  const checkedIngredients = note.structuredData?.ingredients?.filter(i => i.isChecked).length || 0;

  const totalSteps = note.structuredData?.steps?.length || 0;
  const doneSteps = note.structuredData?.steps?.filter(s => s.isDone).length || 0;

  return (
    <div
      onClick={() => onSelect(note)}
      className="group relative bg-[#14161D] hover:bg-[#181B24] border border-[#232733] hover:border-[#383D52] rounded-2xl p-4 transition-all duration-200 cursor-pointer shadow-lg hover:shadow-xl hover:shadow-violet-950/20 flex flex-col justify-between"
    >
      <div>
        {/* Top bar: Category + Actions */}
        <div className="flex items-center justify-between gap-2 mb-2.5">
          <span
            className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${catConfig.badgeBg}`}
          >
            <span>{catConfig.iconEmoji}</span>
            <span>{isFr ? catConfig.labelFr : catConfig.labelEn}</span>
          </span>

          <div className="flex items-center gap-1">
            <button
              title={isFr ? 'Favori' : 'Favorite'}
              onClick={e => onToggleFavorite(e, note.id)}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                note.isFavorite
                  ? 'text-amber-400 hover:text-amber-300'
                  : 'text-slate-600 hover:text-slate-300'
              }`}
            >
              <Star className={`w-4 h-4 ${note.isFavorite ? 'fill-amber-400' : ''}`} />
            </button>

            <button
              title={isFr ? 'Supprimer' : 'Delete'}
              onClick={e => onDelete(e, note.id)}
              className="p-1.5 rounded-lg text-slate-600 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-all cursor-pointer"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Title */}
        <h3 className="font-bold text-slate-100 text-base leading-snug line-clamp-2 group-hover:text-violet-200 transition-colors mb-1.5">
          {note.title}
        </h3>

        {/* Author */}
        {note.author && (
          <div className="text-xs text-violet-400 font-medium mb-2.5 flex items-center gap-1">
            <span>{note.author}</span>
          </div>
        )}

        {/* Summary Snippet */}
        <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed mb-3">
          {note.summary}
        </p>

        {/* Recipe / Workout Meta badges */}
        {(note.structuredData?.prepTime || note.structuredData?.cookTime || note.structuredData?.servings) && (
          <div className="flex flex-wrap items-center gap-2 mb-3 text-[11px] text-slate-400">
            {note.structuredData.prepTime && (
              <span className="flex items-center gap-1 bg-[#1C1F2B] px-2 py-0.5 rounded-md border border-[#282C3D]">
                <Clock className="w-3 h-3 text-cyan-400" /> {note.structuredData.prepTime}
              </span>
            )}
            {note.structuredData.cookTime && (
              <span className="flex items-center gap-1 bg-[#1C1F2B] px-2 py-0.5 rounded-md border border-[#282C3D]">
                <Flame className="w-3 h-3 text-orange-400" /> {note.structuredData.cookTime}
              </span>
            )}
            {note.structuredData.servings && (
              <span className="flex items-center gap-1 bg-[#1C1F2B] px-2 py-0.5 rounded-md border border-[#282C3D]">
                <Users className="w-3 h-3 text-emerald-400" /> {note.structuredData.servings}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Footer Info: Checklist Progress & Tags */}
      <div>
        {totalIngredients > 0 && (
          <div className="flex items-center justify-between text-[11px] mb-2 px-2 py-1 rounded-lg bg-emerald-950/20 border border-emerald-500/20 text-emerald-400 font-medium">
            <span>{isFr ? 'Ingrédients cochés' : 'Ingredients checked'}</span>
            <span>
              {checkedIngredients}/{totalIngredients}
            </span>
          </div>
        )}

        {totalSteps > 0 && totalIngredients === 0 && (
          <div className="flex items-center justify-between text-[11px] mb-2 px-2 py-1 rounded-lg bg-violet-950/20 border border-violet-500/20 text-violet-300 font-medium">
            <span>{isFr ? 'Étapes terminées' : 'Steps completed'}</span>
            <span>
              {doneSteps}/{totalSteps}
            </span>
          </div>
        )}

        {/* Tags */}
        {note.tags && note.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mt-1">
            {note.tags.slice(0, 3).map(tag => (
              <span
                key={tag}
                className="text-[10px] text-slate-500 bg-[#1C1F2B] px-1.5 py-0.5 rounded font-mono"
              >
                #{tag}
              </span>
            ))}
            {note.tags.length > 3 && (
              <span className="text-[10px] text-slate-600 self-center">
                +{note.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
