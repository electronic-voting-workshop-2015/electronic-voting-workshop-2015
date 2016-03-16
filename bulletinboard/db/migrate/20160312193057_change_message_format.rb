class ChangeMessageFormat < ActiveRecord::Migration
  def change
      add_column :messages, :party_id, :integer
      add_column :messages, :recepient_id, :integer
      add_column :messages, :message, :binary, limit: 65535
      add_column :messages, :signature, :binary, limit: 65535
      remove_column :messages, :content, :binary, limit: 65535

      add_index :messages, :party_id
      add_index :messages, :recepient_id
  end
end
