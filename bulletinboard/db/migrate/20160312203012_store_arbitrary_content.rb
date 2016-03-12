class StoreArbitraryContent < ActiveRecord::Migration
  def change
      drop_table :commitments
      drop_table :complaints
      create_table :arbitrary_jsons, id: false do |t|
        t.binary "content", limit: 65535
        t.string "type", index: true
      end
  end
end
