# ClipMemo Backend Proxy

Ce service léger en **Node.js / TypeScript (Fastify)** sert de passerelle sécurisée entre l'application mobile Android **ClipMemo** et l'API **Google Gemini AI**.

---

## Fonctionnalités
- **Sécurisation absolue :** Votre `GEMINI_API_KEY` reste exclusivement sur votre cluster k3s.
- **Protection anti-abus :**
  - Authentification par en-tête `X-App-Key`.
  - Rate limiting automatique (60 requêtes/minute par IP).
- **Fallback automatique :** Essai sur `gemini-2.5-flash`, puis repli sur `gemini-2.0-flash` en cas de 429/503.
- **Support Multimodal & Texte :** Reçoit soit une légende brute, soit un flux vidéo/audio pour transcription et mise en forme.

---

## 1. Démarrage en local (Développement)

```bash
cd backend
cp .env.example .env
# Éditez .env avec votre GEMINI_API_KEY
npm install
npm run dev
```

Test du healthcheck :
```bash
curl http://localhost:3000/health
```

---

## 2. Déploiement Automatisé via GitHub Actions (Recommandé - Modèle Quizzy)

Le déploiement est entièrement automatisé via le workflow [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml).

Il se connecte directement à votre cluster k3s via `KUBECONFIG` (stocké en base64 dans les GitHub Secrets) et applique les manifestes Traefik `IngressRoute` sur `https://clipmemo.dnabil.ovh`.

Pour voir les instructions détaillées de configuration :  
👉 Consultez [Configuration des GitHub Secrets & CI/CD](../docs/ci_cd_github_secrets.md).

---

## 3. Déploiement Manuel sur k3s

Si vous préférez déployer manuellement :

### Étape 1 : Création du Namespace et des Secrets
```bash
kubectl apply -f k8s/01-namespace.yaml

kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: clipmemo-secrets
  namespace: clipmemo
type: Opaque
stringData:
  GEMINI_API_KEY: "VOTRE_CLE_GOOGLE_AI_STUDIO"
  APP_SECRET_KEY: "clipmemo_secret_app_key_2026"
EOF
```

### Étape 2 : Application des manifestes
```bash
kubectl apply -f k8s/03-api-deployment.yaml
kubectl apply -f k8s/04-api-service.yaml
kubectl apply -f k8s/05-ingressroute.yaml
```
