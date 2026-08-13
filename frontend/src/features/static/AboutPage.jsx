import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Box,
  Chip,
  Divider,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import TranslateIcon from '@mui/icons-material/Translate';
import RecommendIcon from '@mui/icons-material/Recommend';
import StarIcon from '@mui/icons-material/Star';
import FavoriteIcon from '@mui/icons-material/Favorite';
import ShieldIcon from '@mui/icons-material/Shield';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import PersonSearchIcon from '@mui/icons-material/PersonSearch';
import EmojiObjectsIcon from '@mui/icons-material/EmojiObjects';
import TrackChangesIcon from '@mui/icons-material/TrackChanges';
import VerifiedIcon from '@mui/icons-material/Verified';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import CodeIcon from '@mui/icons-material/Code';
import PersonIcon from '@mui/icons-material/Person';

// ─── Reusable sub-components ────────────────────────────────────────────────

function SectionHeading({ icon, title, subtitle }) {
  return (
    <Box mb={3}>
      <Box display="flex" alignItems="center" gap={1.2} mb={0.5}>
        {icon && <Box sx={{ color: 'primary.main', display: 'flex' }}>{icon}</Box>}
        <Typography variant="h6" fontWeight={700} color="text.primary">
          {title}
        </Typography>
      </Box>
      {subtitle && (
        <Typography variant="body2" color="text.secondary">
          {subtitle}
        </Typography>
      )}
    </Box>
  );
}

function FeatureCard({ icon, title, description }) {
  return (
    <Box
      sx={{
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 2.5,
        height: '100%',
        transition: 'border-color 0.2s',
        '&:hover': { borderColor: 'primary.main' },
      }}
    >
      <Box sx={{ color: 'primary.main', mb: 1 }}>{icon}</Box>
      <Typography variant="body1" fontWeight={600} mb={0.5} color="text.primary">
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" lineHeight={1.65}>
        {description}
      </Typography>
    </Box>
  );
}

function ValueCard({ icon, title, description }) {
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 2,
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 2.5,
      }}
    >
      <Box sx={{ color: 'primary.main', flexShrink: 0, mt: 0.2 }}>{icon}</Box>
      <Box>
        <Typography variant="body1" fontWeight={600} mb={0.4} color="text.primary">
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary" lineHeight={1.65}>
          {description}
        </Typography>
      </Box>
    </Box>
  );
}

// ─── Page ────────────────────────────────────────────────────────────────────

function AboutPage() {
  const { hash } = useLocation();

  useEffect(() => {
    if (!hash) return;
    const el = document.getElementById(hash.slice(1));
    if (el) {
      // Small delay lets the page paint before scrolling
      setTimeout(() => el.scrollIntoView({ behavior: 'smooth', block: 'start' }), 80);
    }
  }, [hash]);

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={6}>

      {/* ── Hero ── */}
      <Box
        sx={{
          bgcolor: 'background.paper',
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 3,
          p: { xs: 3, md: 5 },
          mb: 5,
          textAlign: 'center',
        }}
      >
        <Box display="flex" justifyContent="center" mb={2}>
          <LocalLibraryIcon sx={{ fontSize: 52, color: 'primary.main' }} />
        </Box>
        <Typography variant="h3" fontWeight={800} mb={1.5} letterSpacing={-0.5}>
          BookStore
        </Typography>
        <Typography
          variant="h6"
          color="text.secondary"
          fontWeight={400}
          maxWidth={580}
          mx="auto"
          lineHeight={1.7}
        >
          Your destination for multilingual books — discover, explore, and fall in love with
          reading across languages, genres, and cultures.
        </Typography>
      </Box>

      {/* ── Mission & Vision ── */}
      <Box
        display="grid"
        gridTemplateColumns={{ xs: '1fr', md: '1fr 1fr' }}
        gap={3}
        mb={5}
      >
        <Box
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: 3,
          }}
        >
          <Box display="flex" alignItems="center" gap={1} mb={1.5}>
            <TrackChangesIcon sx={{ color: 'primary.main' }} />
            <Typography variant="h6" fontWeight={700}>Our Mission</Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" lineHeight={1.75}>
            To make great books accessible to every reader — regardless of language, genre, or
            budget. We believe reading is a universal right, and BookStore is built to celebrate
            that across English, Tamil, Hindi, Malayalam, Kannada, Telugu, Marathi, and beyond.
          </Typography>
        </Box>

        <Box
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: 3,
          }}
        >
          <Box display="flex" alignItems="center" gap={1} mb={1.5}>
            <EmojiObjectsIcon sx={{ color: 'primary.main' }} />
            <Typography variant="h6" fontWeight={700}>Our Vision</Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" lineHeight={1.75}>
            To become the most trusted and beloved online bookstore for Indian and global readers —
            a platform where every book finds its reader, every author finds their audience, and
            every visit feels like stepping into a library you never want to leave.
          </Typography>
        </Box>
      </Box>

      <Divider sx={{ mb: 5 }} />

      {/* ── What We Offer ── */}
      <Box mb={5}>
        <SectionHeading
          icon={<AutoStoriesIcon />}
          title="What We Offer"
          subtitle="Everything you need for a complete, joyful reading experience — all in one place."
        />
        <Box
          display="grid"
          gridTemplateColumns={{ xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' }}
          gap={2.5}
        >
          <FeatureCard
            icon={<TranslateIcon />}
            title="Multilingual Catalogue"
            description="Browse thousands of books in English, Hindi, Tamil, Telugu, Malayalam, Kannada, and more. One platform, many voices."
          />
          <FeatureCard
            icon={<RecommendIcon />}
            title="Personalised Recommendations"
            description="Discover books tailored to your tastes through smart recommendations based on genres, authors, and reading history."
          />
          <FeatureCard
            icon={<PersonSearchIcon />}
            title="Author Discovery"
            description="Explore dedicated author profiles, browse their complete works, and follow writers you love across every genre."
          />
          <FeatureCard
            icon={<StarIcon />}
            title="Reviews & Ratings"
            description="Read honest community reviews, share your own thoughts, and make confident purchase decisions backed by reader opinions."
          />
          <FeatureCard
            icon={<FavoriteIcon />}
            title="Wishlist"
            description="Save books you want to read later to your personal wishlist and never lose track of your next great read."
          />
          <FeatureCard
            icon={<ShieldIcon />}
            title="Secure Shopping"
            description="Shop with confidence using JWT-secured accounts, encrypted sessions, and multiple trusted payment methods."
          />
          <FeatureCard
            icon={<LocalShippingIcon />}
            title="Order Management"
            description="Track your purchases, view order history, and stay updated on delivery estimates — all from your personal dashboard."
          />
          <FeatureCard
            icon={<AutoStoriesIcon />}
            title="Multiple Formats"
            description="Choose between Paperback, Hardcover, and eBook editions based on your preference, mood, and budget."
          />
          <FeatureCard
            icon={<VerifiedIcon />}
            title="Curated Genres"
            description="From Romance and Mystery to Philosophy, Poetry, and Children's books — 20 carefully curated genres for every kind of reader."
          />
        </Box>
      </Box>

      <Divider sx={{ mb: 5 }} />

      {/* ── Our Values ── */}
      <Box mb={5}>
        <SectionHeading
          icon={<VerifiedIcon />}
          title="Our Values"
          subtitle="The principles that guide everything we build and every decision we make."
        />
        <Stack spacing={2}>
          <ValueCard
            icon={<AutoStoriesIcon />}
            title="Books First"
            description="Every feature, every design decision, every line of code exists to serve one purpose — helping readers discover and enjoy great books."
          />
          <ValueCard
            icon={<TranslateIcon />}
            title="Inclusivity"
            description="Language should never be a barrier to reading. We actively champion multilingual content and diverse voices from India and around the world."
          />
          <ValueCard
            icon={<ShieldIcon />}
            title="Trust & Security"
            description="Your data, your privacy, and your transactions are handled with the highest standards of security and transparency."
          />
          <ValueCard
            icon={<EmojiObjectsIcon />}
            title="Continuous Improvement"
            description="We are always learning, always improving. BookStore evolves with our readers' needs, feedback, and the latest in technology."
          />
        </Stack>
      </Box>

      <Divider sx={{ mb: 5 }} />

      {/* ── Our Journey ── */}
      <Box mb={5}>
        <SectionHeading
          icon={<TrackChangesIcon />}
          title="Our Journey"
        />
        <Box
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: 3,
          }}
        >
          <Typography variant="body1" color="text.secondary" lineHeight={1.8} mb={2}>
            BookStore began as a vision to create a modern, beautifully designed online bookstore
            that would feel as welcoming as your favourite neighbourhood bookshop — but with the
            power and convenience of a full-scale e-commerce platform.
          </Typography>
          <Typography variant="body1" color="text.secondary" lineHeight={1.8}>
            From a blank canvas, we built a complete product: a robust Spring Boot REST API, a
            responsive React frontend, a curated catalogue of books across 6 languages and 20 genres,
            a full purchase flow, user accounts, wishlists, reviews, and much more. Every feature
            was designed with the reader's experience at its heart — because great software, like a
            great book, should feel effortless to use.
          </Typography>
        </Box>
      </Box>

      <Divider sx={{ mb: 5 }} />

      {/* ── About the Creator ── */}
      <Box id="about-creator" mb={5}>
        <SectionHeading
          icon={<PersonIcon sx={{ fontSize: 22 }} />}
          title="About the Creator"
        />
        <Box
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: 3,
          }}
        >
          <Box display="flex" alignItems="center" gap={1.5} mb={2.5}>
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: '50%',
                bgcolor: 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <PersonIcon sx={{ color: '#fff', fontSize: 26 }} />
            </Box>
            <Box>
              <Typography variant="h6" fontWeight={700}>
                <strong>Ramkumar K</strong>{' '}
                <Typography component="span" variant="body2" color="text.secondary">
                  (Ram)
                </Typography>
              </Typography>
              <Typography variant="body2" color="primary.main" fontWeight={500}>
                Application Developer &amp; Applied AI Specialist · IBM
              </Typography>
            </Box>
          </Box>

          <Typography variant="body1" color="text.secondary" lineHeight={1.8} mb={2}>
            <strong>Ram</strong> is an Application Developer and Applied AI Specialist at IBM with
            4 years of professional experience building enterprise software and AI-powered
            applications. He is passionate about clean architecture, developer experience, and the
            intersection of engineering and artificial intelligence.
          </Typography>

          <Box
            sx={{
              borderLeft: '3px solid',
              borderColor: 'primary.main',
              pl: 2,
              mb: 2,
            }}
          >
            <Box display="flex" alignItems="flex-start" gap={1} mb={1.5}>
              <CodeIcon sx={{ color: 'text.secondary', mt: 0.25, fontSize: 18, flexShrink: 0 }} />
              <Typography variant="body2" color="text.secondary" lineHeight={1.75}>
                BookStore was designed and developed by <strong>Ram</strong> during personal time as
                a learning and portfolio project — to demonstrate full-stack engineering across a
                realistic, production-grade e-commerce domain.
              </Typography>
            </Box>
            <Box display="flex" alignItems="flex-start" gap={1}>
              <SmartToyIcon sx={{ color: 'text.secondary', mt: 0.25, fontSize: 18, flexShrink: 0 }} />
              <Typography variant="body2" color="text.secondary" lineHeight={1.75}>
                The project was built in close collaboration with{' '}
                <strong>Bob</strong>, an AI coding partner — combining <strong>Ram</strong>'s
                engineering judgement, domain knowledge, and product thinking with AI-assisted
                implementation to deliver a polished, professional application.
              </Typography>
            </Box>
          </Box>

          <Box display="flex" flexWrap="wrap" gap={1}>
            {['Spring Boot 3', 'Java 17', 'PostgreSQL', 'React 18', 'Material UI v5', 'JWT Auth', 'TanStack Query', 'Vite'].map((tech) => (
              <Chip key={tech} label={tech} size="small" variant="outlined" sx={{ borderColor: 'divider', fontSize: 11 }} />
            ))}
          </Box>
        </Box>
      </Box>

      <Divider sx={{ mb: 3 }} />

      <Typography variant="caption" color="text.secondary" display="block" textAlign="center">
        BookStore is a personal portfolio project and is not a real commercial product.
        All book data, prices, and content are fictional and for demonstration purposes only.
      </Typography>
    </Box>
  );
}

export default AboutPage;
