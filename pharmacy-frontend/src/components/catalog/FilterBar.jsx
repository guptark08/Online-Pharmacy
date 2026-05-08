import Button from "../common/Button";
import FormField from "../forms/FormField";

export default function FilterBar({
  filters,
  categories = [],
  onInputChange,
  onSubmit,
  onReset,
}) {
  return (
    <form className="filter-bar" onSubmit={onSubmit}>
      <FormField label="Search">
        <input
          name="search"
          value={filters.search}
          onChange={onInputChange}
          placeholder="Type medicine name"
        />
      </FormField>

      <FormField label="Category">
        <select name="categoryId" value={filters.categoryId} onChange={onInputChange}>
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </FormField>

      <label className="checkbox-field">
        <input
          type="checkbox"
          name="prescriptionOnly"
          checked={filters.prescriptionOnly}
          onChange={onInputChange}
        />
        <span>Prescription medicines only</span>
      </label>

      <div className="filter-bar__actions">
        <Button type="submit">Apply</Button>
        <Button type="button" variant="ghost" onClick={onReset}>
          Reset
        </Button>
      </div>
    </form>
  );
}
