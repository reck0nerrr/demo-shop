import { useState } from "react";

export default function ImageCarousel({ images = [], alt = "" }) {
  const [index, setIndex] = useState(0);

  if (images.length === 0) {
    return (
      <div className="carousel">
        <div className="carousel-placeholder">{alt.charAt(0)}</div>
      </div>
    );
  }

  function prev(e) {
    e.stopPropagation();
    setIndex((i) => (i === 0 ? images.length - 1 : i - 1));
  }

  function next(e) {
    e.stopPropagation();
    setIndex((i) => (i === images.length - 1 ? 0 : i + 1));
  }

  return (
    <div className="carousel">
      <img src={images[index]} alt={alt} />
      {images.length > 1 && (
        <>
          <button className="carousel-arrow carousel-prev" onClick={prev} aria-label="Previous image">‹</button>
          <button className="carousel-arrow carousel-next" onClick={next} aria-label="Next image">›</button>
          <span className="carousel-counter">{index + 1}/{images.length}</span>
        </>
      )}
    </div>
  );
}