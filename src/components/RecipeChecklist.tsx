import React from 'react';
import { Check, CheckCircle2, ShoppingBag } from 'lucide-react';
import { IngredientItem } from '../types/note';
import confetti from 'canvas-confetti';

interface RecipeChecklistProps {
  ingredients: IngredientItem[];
  onToggleIngredient: (index: number) => void;
  language: 'fr' | 'en';
}

export const RecipeChecklist: React.FC<RecipeChecklistProps> = ({
  ingredients,
  onToggleIngredient,
  language,
}) => {
  const isFr = language === 'fr';

  if (!ingredients || ingredients.length === 0) return null;

  const checkedCount = ingredients.filter(i => i.isChecked).length;
  const totalCount = ingredients.length;
  const progressPercent = Math.round((checkedCount / totalCount) * 100);

  const handleToggle = (index: number) => {
    const isNowChecked = !ingredients[index].isChecked;
    onToggleIngredient(index);

    // If this completes all ingredients, trigger celebration!
    if (isNowChecked && checkedCount + 1 === totalCount) {
      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.7 },
        colors: ['#10B981', '#34D399', '#8B5CF6', '#FBBF24'],
      });
    }
  };

  return (
    <div className="bg-[#14161D] rounded-2xl border border-[#232733] p-4.5 mb-5 shadow-lg">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <ShoppingBag className="w-4 h-4" />
          </div>
          <h3 className="font-bold text-sm text-slate-100">
            {isFr ? 'Ingrédients & Checklist' : 'Ingredients Checklist'}
          </h3>
        </div>
        <div className="flex items-center gap-2 text-xs">
          <span className="font-semibold text-emerald-400">
            {checkedCount}/{totalCount}
          </span>
          <span className="text-slate-400">({progressPercent}%)</span>
        </div>
      </div>

      {/* Progress bar */}
      <div className="w-full bg-[#1C1F2B] h-1.5 rounded-full overflow-hidden mb-4">
        <div
          className="bg-gradient-to-r from-emerald-500 to-teal-400 h-full rounded-full transition-all duration-300"
          style={{ width: `${progressPercent}%` }}
        />
      </div>

      <div className="space-y-2">
        {ingredients.map((item, idx) => {
          const qty = [item.amount, item.unit].filter(Boolean).join(' ');
          const checked = !!item.isChecked;

          return (
            <div
              key={idx}
              onClick={() => handleToggle(idx)}
              className={`flex items-start gap-3 p-2.5 rounded-xl transition-all cursor-pointer select-none border ${
                checked
                  ? 'bg-emerald-950/20 border-emerald-500/30 text-slate-400'
                  : 'bg-[#1C1F2B]/60 hover:bg-[#1C1F2B] border-transparent text-slate-200'
              }`}
            >
              <div
                className={`w-5 h-5 mt-0.5 rounded-lg flex items-center justify-center border transition-all shrink-0 ${
                  checked
                    ? 'bg-emerald-500 border-emerald-500 text-slate-950'
                    : 'border-slate-600 bg-slate-800/80 hover:border-emerald-400'
                }`}
              >
                {checked && <Check className="w-3.5 h-3.5 stroke-[3]" />}
              </div>

              <div className="flex-1 text-sm leading-tight">
                {qty && (
                  <span
                    className={`font-bold mr-1.5 ${
                      checked ? 'text-emerald-500/80 line-through' : 'text-emerald-400 font-mono'
                    }`}
                  >
                    {qty}
                  </span>
                )}
                <span className={checked ? 'line-through text-slate-500' : ''}>
                  {item.name}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
