export default function Pager({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;
  return (
    <div className="pager">
      <button className="secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>← Prev</button>
      <span className="pager-count">Page {page + 1} of {totalPages}</span>
      <button className="secondary" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>Next →</button>
    </div>
  );
}