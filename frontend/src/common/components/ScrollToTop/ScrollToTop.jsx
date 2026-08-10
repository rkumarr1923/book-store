import { useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';

/**
 * ScrollToTop
 *
 * Resets the window scroll position to (0, 0) on every pathname change.
 * Hash navigation (e.g. /about#about-creator) is intentionally preserved:
 * when the URL contains a hash the browser / AboutPage useEffect handles
 * scrolling to the anchor, so we skip the reset in that case.
 */
function ScrollToTop() {
  const { pathname, hash } = useLocation();
  // Track the previous pathname so we only reset when the route changes,
  // not when only the hash or query string changes on the same page.
  const prevPathname = useRef(null);

  useEffect(() => {
    if (pathname === prevPathname.current) return; // same page — don't reset
    prevPathname.current = pathname;

    if (hash) return; // navigating to an anchor — let the page handle scrolling

    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
  }, [pathname, hash]);

  return null;
}

export default ScrollToTop;
