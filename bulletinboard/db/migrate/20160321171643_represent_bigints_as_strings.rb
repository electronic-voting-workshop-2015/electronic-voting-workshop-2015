class RepresentBigintsAsStrings < ActiveRecord::Migration
  def up
      change_column :public_keys, :first, :string
      change_column :public_keys, :second, :string
  end

  def down
      change_column :public_keys, :first, :integer, limit: 8
      change_column :public_keys, :second, :integer, limit: 8
  end
end
