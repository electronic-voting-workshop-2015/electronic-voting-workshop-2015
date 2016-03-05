class CreatePublicKey < ActiveRecord::Migration
  def change
      create_table :public_keys, id: false do |t|
          t.primary_key :party_id
          t.integer :first, limit: 8
          t.integer :second, limit: 8
      end
  end
end
