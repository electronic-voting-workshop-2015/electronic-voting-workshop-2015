class CreateMessages < ActiveRecord::Migration
  def change
      create_table :messages, id: false do |t|
          t.binary :content
      end
  end
end
